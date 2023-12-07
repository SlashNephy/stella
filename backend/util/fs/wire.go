package fs

import "github.com/google/wire"

var Set = wire.NewSet(
	NewRealFileSystem,
	wire.Bind(new(FileSystem), new(*RealFileSystem)),
)
