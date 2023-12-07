package clock

import "time"

type FakeClock struct {
	now time.Time
}

func NewFakeClock(now time.Time) *FakeClock {
	return &FakeClock{
		now: now,
	}
}

func (r *FakeClock) Now() time.Time {
	return r.now
}

var _ Clock = new(FakeClock)
