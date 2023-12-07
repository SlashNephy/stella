package logger

import (
	"log/slog"
	"os"

	"github.com/pkg/errors"
)

type Config struct {
	LogLevel string `env:"LOG_LEVEL" envDefault:"info"`
}

func NewLogger(config *Config) (*slog.Logger, error) {
	var logLevel slog.Level
	if err := logLevel.UnmarshalText([]byte(config.LogLevel)); err != nil {
		return nil, errors.WithStack(err)
	}

	return slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{
		Level: &logLevel,
	})), nil
}
