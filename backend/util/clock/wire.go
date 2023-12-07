package clock

import "github.com/google/wire"

var Set = wire.NewSet(
	NewRealClock,
	wire.Bind(new(Clock), new(*RealClock)),
)
