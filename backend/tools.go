//go:build tools
// +build tools

package main

import (
	_ "github.com/cosmtrek/air"
	_ "github.com/google/wire/cmd/wire"
	_ "github.com/utgwkk/bulkmockgen/cmd/bulkmockgen"
	_ "go.uber.org/mock/mockgen"
)
