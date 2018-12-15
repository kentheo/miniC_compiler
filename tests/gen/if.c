

int a;
void main(){
	int x;
	char c;
	a = 4;
	x = 5;
	c = 'v';
	if (x >= a) {
		print_i(x);
	} else {
		print_i(1000);	
	}
	if (x - 1 == a){
		print_c(c);
	}
	c = 'c';
}


// should output 5v
