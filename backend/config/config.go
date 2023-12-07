package config

import (
	"github.com/caarlos0/env/v10"
	"github.com/cockroachdb/errors"
	"github.com/google/wire"
	_ "github.com/joho/godotenv/autoload"

	"github.com/SlashNephy/stella/infrastructure/database"
	"github.com/SlashNephy/stella/util/logger"
	"github.com/SlashNephy/stella/web"
)

type Config struct {
	ServerConfig      web.ServerConfig
	MongoClientConfig database.MongoClientConfig `envPrefix:"MONGO_"`
	LoggerConfig      logger.Config
}

func Load() (*Config, error) {
	var config Config
	if err := env.Parse(&config); err != nil {
		return nil, errors.WithStack(err)
	}

	return &config, nil
}

var Set = wire.NewSet(
	wire.FieldsOf(
		new(*Config),
		"ServerConfig",
		"MongoClientConfig",
		"LoggerConfig",
	),
)
