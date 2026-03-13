cmake_minimum_required(VERSION 3.15)
project(HelloCPP VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(GTest REQUIRED)

# Library target
add_library(hello_lib src/hello.cpp)
target_include_directories(hello_lib PUBLIC ${CMAKE_SOURCE_DIR}/include)

# Executable target
add_executable(hello_app src/main.cpp)
target_link_libraries(hello_app PRIVATE hello_lib)

# Test target
enable_testing()
add_executable(hello_test test/test_hello.cpp)
target_link_libraries(hello_test PRIVATE hello_lib GTest::gtest GTest::gtest_main)
add_test(NAME HelloTest COMMAND hello_test)

# Install rules for packaging
install(TARGETS hello_app RUNTIME DESTINATION bin)
install(TARGETS hello_lib ARCHIVE DESTINATION lib LIBRARY DESTINATION lib)
install(DIRECTORY include/ DESTINATION include)
