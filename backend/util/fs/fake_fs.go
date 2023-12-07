package fs

type FakeFileSystem struct {
	exists bool
}

func NewFakeFileSystem(exists bool) *FakeFileSystem {
	return &FakeFileSystem{
		exists: exists,
	}
}

func (fs *FakeFileSystem) Exists(_ string) (bool, error) {
	return fs.exists, nil
}

var _ FileSystem = new(FakeFileSystem)
