// Code generated by Wire. DO NOT EDIT.

//go:generate go run github.com/google/wire/cmd/wire
//go:build !wireinject
// +build !wireinject

package main

import (
	"context"
	"github.com/SlashNephy/stella/config"
	"github.com/SlashNephy/stella/infrastructure/database"
	"github.com/SlashNephy/stella/web"
	"github.com/SlashNephy/stella/web/controller"
)

// Injectors from wire.go:

func InitializeServer(ctx context.Context, cfg *config.Config, mongo *database.MongoClient) (*web.Server, error) {
	serverConfig := &cfg.ServerConfig
	controllerController := controller.NewController()
	server := web.NewServer(serverConfig, controllerController)
	return server, nil
}