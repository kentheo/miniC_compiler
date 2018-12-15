void main(){
	int a;
	int b;
	int c;
	int d;
	a = 3;
	b = 4;
	c = 5;
	d = 6;
	if ((a<b) && (c<d)){
		print_i(1000);
	} else {
		print_i(2000);
	}
}
// output of this should be 1000
// check (a<b) --> check the returned register
// if it's equal to 0 then don't go to next stmt
// if it's equal to 1 then check next stmt
// then follow to body
