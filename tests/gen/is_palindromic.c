void main() {
  int i; // temporary (pointer) variable
  int n; // The number in question
  int reverse; // the reverse of the number

  // Read an integer from stdin
  print_s((char*)"Enter integer> "); n = read_i();

  // Initial reverse; and pointer variable
  reverse = 0;
  i = n;

  while (i != 0) {
    reverse = (reverse * 10) + (i % 10);
    i       = i / 10;
  }

  print_i(n);
  if (n == reverse) print_s((char*)" is palindromic.\n");
  else print_s((char*)" is not palindromic.\n");
}
