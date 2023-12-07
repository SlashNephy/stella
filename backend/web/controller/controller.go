package controller

import "github.com/labstack/echo/v4"

type Controller struct {
}

func NewController() *Controller {
	return &Controller{}
}

func (co *Controller) RegisterRoutes(e *echo.Echo) {
	e.GET("/health", co.HandleGetHealth)
}
