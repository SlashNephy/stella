package web

import (
	"context"

	"github.com/cockroachdb/errors"
	"github.com/labstack/echo/v4"

	"github.com/SlashNephy/stella/web/controller"
)

type Server struct {
	e      *echo.Echo
	config *ServerConfig
}

type ServerConfig struct {
	ServerAddress string `env:"SERVER_ADDRESS" envDefault:":8080"`
}

func NewServer(
	config *ServerConfig,
	controller *controller.Controller,
) *Server {
	e := echo.New()
	e.HideBanner = true

	controller.RegisterRoutes(e)

	return &Server{
		e:      e,
		config: config,
	}
}

func (s *Server) Start() error {
	err := s.e.Start(s.config.ServerAddress)
	return errors.WithStack(err)
}

func (s *Server) Shutdown(ctx context.Context) error {
	err := s.e.Shutdown(ctx)
	return errors.WithStack(err)
}
