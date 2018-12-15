int checker(int a, int b){
	return a-b;

}

int foo(int a, int b){
	int x;	
	x = 4;
	return x+a+b;
}

void main() {
  int a;
  int b;
  a = 2;
  b = 3;
  print_i(checker(10,8));
  //n = read_i();
  a = foo(3,4);
  print_i(a);
  b = foo(a,3);
  print_s((char*)"\n");
  print_i(b);
  //print_i(x);
}
//output should be 211  18
