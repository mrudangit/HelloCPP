#include <iostream>
#include "hello.h"

int main() {
    std::cout << hello::greet("World") << std::endl;
    return 0;
}
