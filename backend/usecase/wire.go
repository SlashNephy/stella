package usecase

import (
	"github.com/google/wire"

	"github.com/SlashNephy/stella/infrastructure/external"
	"github.com/SlashNephy/stella/infrastructure/repository"
)

var Set = wire.NewSet(
	repository.Set,
	external.Set,
)
