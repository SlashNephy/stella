package controller

import (
	"net/http"

	"github.com/labstack/echo/v4"
)

func (co *Controller) HandleGetHealth(c echo.Context) error {
	return c.JSON(http.StatusOK, map[string]bool{
		"ok": true,
	})
}
