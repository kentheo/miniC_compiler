
int a;
void main(){
	int b;
	a = 2;	
	b = 5;
	while (a<b){
		print_i(a);
		
		while (b >= 3){
			print_i(1000);
			b = b - 1;
		}	
		a = a + 1;
	}

}

// should output 2100010001000
