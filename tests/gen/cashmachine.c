// Simple ATM model for a bank with only one customer.
// Provides two services: Withdraw and deposit.
// Written by Daniel Hillerström
#include "io.h"

// The account balance
int balance;

// Deposit money into the account
int deposit(int amount) {
  balance = balance + amount;
  return amount;
}

// Withdraw money from the account
// Restriction: account - amount must be positive.
int withdraw(int amount) {
  if (balance - amount >= 0) {
    amount = -1 * amount;
    deposit(amount);
    return -1 * amount;
  } else {
    return -1;
  }
}

// Computes the number of units of unit_size to dispense for amount.
int units(int amount, int unit_size) {
  return amount / unit_size;
}

// Dispenses the amount
void dispense(int amount) {
  int hundreds; int fifties; int twenties; int tens;
  int fives; int ones;
  hundreds = 100; fifties = 50; twenties = 20; tens = 10; fives = 5; ones = 1;

  hundreds = units(amount, hundreds);
  amount   = amount % 100;

  fifties  = units(amount, fifties);
  amount   = amount % 50;

  twenties = units(amount, twenties);
  amount   = amount % 20;

  tens     = units(amount, tens);
  amount   = amount % 10;

  fives    = units(amount, fives);
  amount   = amount % 5;

  ones     = units(amount, ones);

  print_s((char*)"********* Dispensing *********\n");
  print_i(hundreds); print_s((char*)"x100 bills\n");
  print_i(fifties);  print_s((char*)"x 50 bills\n");
  print_i(twenties); print_s((char*)"x 20 bills\n");
  print_i(tens);     print_s((char*)"x 10 bills\n");
  print_i(fives);    print_s((char*)"x  5 bills\n");
  print_i(ones);     print_s((char*)"x  1 coins\n");
  print_s((char*)"******************************\n");
}

// Select either withdraw or deposit.
int select_service() {
  int service;
  print_s((char*)"SERVICES:\n");
  print_s((char*)"1: Withdraw\n");
  print_s((char*)"2: Deposit\n");
  print_s((char*)"Enter service number> ");
  service = read_i();
  read_c(); // consume enter
  return service;
}

// Use a particular service
void use_service(int service) {
  int amount; int status;
  if (service < 1) print_s((char*)"error: Invalid service.\n");
  else if (service > 2) print_s((char*)"error: Invalid service.\n");
  else {
    if (service == 1) {
      print_s((char*)"info: Service 'Withdraw' selected\n");
      print_s((char*)"Your current balance is: ");print_i(balance);print_s((char*)"\n");
      print_s((char*)"Enter amount to withdraw> ");
      amount = read_i();
      read_c(); // consume enter
      if (amount >= 0) {
	status = withdraw(amount);
	if (status < 0) {
	  print_s((char*)"error: You got an insufficient balance. Your basic account does not allow overdrafts.\n");
	  print_s((char*)"       Consider upgrading to a premium account.\n");
	} else {
	  dispense(amount);
	}
      } else {
	print_s((char*)"error: You cannot withdraw a negative amount!\n");
      }
    } else {
      print_s((char*)"info: Service 'Deposit' selected\n");
      print_s((char*)"Your current balance is: ");print_i(balance);print_s((char*)"\n");
      print_s((char*)"Enter amount to deposit> ");
      amount = read_i();
      read_c(); // consume enter
      if (amount <= 0) {
	print_s((char*)"error: Cannot deposit non-positive amount.\n");
      } else {
	status = deposit(amount);
	if (status < 0) {
	  print_s((char*)"error: Could not deposit money, please try again.\n");
	} else {
	  print_s((char*)"Successfully deposited ");print_i(amount);print_s((char*)".\n");
	}
      }
    }
  }
}

// Ask whether to the user wishes to do another transaction
int new_transaction() {
  char yesno;
  print_s((char*)"Do you wish to carry out another transaction? (y/n)> ");
  yesno = read_c();
  read_c(); // consume enter
  if (yesno == 'y') return 1;
  else if (yesno == 'Y') return 1;
  else return 0;
}

// Program entry point
void main() {
  int transaction; int service;

  balance = 1000;  // Initial balance
  transaction = 1; // Indicates a session is ongoing
 
  while (transaction) { // while in a session
    service = select_service();
    use_service(service);
    transaction = new_transaction();
  }
}
