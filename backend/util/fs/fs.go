package fs

type FileSystem interface {
	Exists(path string) (bool, error)
}
