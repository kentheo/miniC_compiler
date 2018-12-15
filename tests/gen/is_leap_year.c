#include "minic-stdlib.h"

void main() {
  int year;

  print_s((char*)"Enter year> ");
  year = read_i();

  print_i(year);
  if ( year % 400 == 0 ) print_s((char*)" is a leap year.\n");
  else if ( year % 100 == 0) print_s((char*)" is not a leap year.\n");
  else if ( year % 4 == 0 ) print_s((char*)" is a leap year.\n");
  else print_s((char*)" is not a leap year.\n");
}
