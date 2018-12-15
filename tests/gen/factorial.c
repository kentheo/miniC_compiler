#include "minic-stdlib.h"

int factorial(int n) {
  int m;
  if (n == 0) {
    print_s((char*)"1");
    return 1;
  } else if (n == 1) {
    print_s((char*)"1");
    return 1;
  } else {
    print_i(n); print_s((char*)" * ");
    m = n - 1;
    return n * factorial(m);
  }
}

void main() {
  int n;
  int x;
  n = read_i();
  x = factorial(n);
  print_s((char*)"\n");
  print_i(x);
}
