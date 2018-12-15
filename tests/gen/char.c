void solve_toh(int ndisks, char a, char c, char b){
	if (ndisks > 0) {
    		ndisks = ndisks - 1;
		solve_toh(ndisks, a, c, b);
	}

}


void main(){
	int ndisks;
  char a; char b; char c;
  a = 'A'; b = 'B'; c = 'C';
	ndisks = read_i();	
	solve_toh(ndisks, a, c, b);
}
