package fs

import (
	"os"

	"github.com/cockroachdb/errors"
)

type RealFileSystem struct {
	existsFn func(path string) (bool, error)
}

func NewRealFileSystem() *RealFileSystem {
	return &RealFileSystem{}
}

func (fs *RealFileSystem) Exists(path string) (bool, error) {
	if _, err := os.Stat(path); err != nil {
		if os.IsNotExist(err) {
			return false, nil
		}

		return false, errors.WithStack(err)
	}

	return true, nil
}

var _ FileSystem = new(RealFileSystem)
