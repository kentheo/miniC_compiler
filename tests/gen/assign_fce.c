int foo(int a) {
    	return (a + 2) / 5;
}

int bar(int a, int b){
	return a+b;
}

int main() {
	int x;
	int z;	
	x = foo(3);
	print_i(x);
	print_s((char*)"\n");
	z = bar(9,5);
	print_i(z);
}
