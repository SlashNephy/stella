package web

import (
	"github.com/google/wire"

	"github.com/SlashNephy/stella/web/controller"
)

var Set = wire.NewSet(
	NewServer,
	controller.NewController,
)
