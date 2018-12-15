//vardecl
int x;
int y;
void main(){
	x = 6;
	y = 4;	
	while ( x >= y ){
		print_i(x+y*3);
		x = x - 1;	
	}
}