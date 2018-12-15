

void main(){
	int a;	
	a = 5;
	while (a >= 3){
		print_i(a);
		a = a - 1;
	}
	if (a == 5){
		print_i(1000);
	} else {
		print_i(2000);
	}
	while(a < 8){
		print_i(a+1);
		a = a + 1;
	}
}

// should output 5432000345678
