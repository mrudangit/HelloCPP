from conan import ConanFile


class HelloCPPConan(ConanFile):
    name = "hellocpp"
    version = "1.0.0"
    settings = "os", "compiler", "build_type", "arch"
    requires = ("gtest/1.15.0",)
    generators = "CMakeToolchain", "CMakeDeps"

    def configure(self):
        self.options["gtest"].shared = False
