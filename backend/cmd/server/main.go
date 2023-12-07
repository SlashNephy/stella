package main

import (
	"context"
	"errors"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/SlashNephy/stella/config"
	"github.com/SlashNephy/stella/infrastructure/database"
)

func main() {
	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGTERM, os.Interrupt)
	defer stop()

	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	mongo, err := database.OpenMongoClient(ctx, &cfg.MongoClientConfig)
	if err != nil {
		log.Fatalf("failed to open mongo client: %v", err)
	}
	defer func(mongo *database.MongoClient, ctx context.Context) {
		if err = mongo.Disconnect(ctx); err != nil {
			log.Fatalf("failed to disconnect mongo client: %v", err)
		}
	}(mongo, ctx)

	server, err := InitializeServer(ctx, cfg, mongo)
	if err != nil {
		panic(err)
	}

	shutdown := make(chan struct{})
	go func() {
		<-ctx.Done()
		ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()

		if err = server.Shutdown(ctx); err != nil {
			log.Fatalf("failed to shutdown server: %v", err)
		}
		close(shutdown)
	}()

	if err = server.Start(); err != nil {
		if !errors.Is(err, http.ErrServerClosed) {
			log.Fatalf("failed to start server: %v", err)
		}
	}

	<-shutdown
}
