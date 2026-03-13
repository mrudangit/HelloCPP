#include <gtest/gtest.h>
#include "hello.h"

TEST(HelloTest, GreetReturnsCorrectString) {
    EXPECT_EQ(hello::greet("World"), "Hello, World!");
}

TEST(HelloTest, GreetWithEmptyName) {
    EXPECT_EQ(hello::greet(""), "Hello, !");
}

TEST(HelloTest, GreetWithCustomName) {
    EXPECT_EQ(hello::greet("Conan"), "Hello, Conan!");
}
