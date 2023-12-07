//go:build wireinject
// +build wireinject

package main

import (
	"context"

	"github.com/google/wire"

	"github.com/SlashNephy/stella/config"
	"github.com/SlashNephy/stella/infrastructure/database"
	"github.com/SlashNephy/stella/web"
)

func InitializeServer(ctx context.Context, cfg *config.Config, mongo *database.MongoClient) (*web.Server, error) {
	wire.Build(
		config.Set,
		web.Set,
		// usecase.Set,
	)

	return nil, nil
}
