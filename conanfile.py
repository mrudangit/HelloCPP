from conan import ConanFile
from conan.tools.cmake import CMakeToolchain, CMakeDeps, cmake_layout


class HelloCPPConan(ConanFile):
    name = "hellocpp"
    version = "1.0.0"
    settings = "os", "compiler", "build_type", "arch"
    requires = ("gtest/1.15.0",)
    generators = "CMakeToolchain", "CMakeDeps"

    def layout(self):
        cmake_layout(self)

    def configure(self):
        self.options["gtest"].shared = False
