

int bar(int a, int b){
	int c;
	int d;
	c = 5;
	d = 20;
	return d+a+c;
}

void main() {
	int n;
	int x;
	int y;
	n = read_i();
	print_i(bar(n,2));   // output = 32
	x = 8;
	print_i(x);
	print_s((char*)"\n");
	print_i(bar(100,55));   // output = 125
	y = bar(1,55);
}

// output 328\n125
