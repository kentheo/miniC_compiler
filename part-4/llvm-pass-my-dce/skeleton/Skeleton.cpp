#define DEBUG_TYPE "opCounter"
#include "llvm/Pass.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Instructions.h"
#include "llvm/IR/InstIterator.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Transforms/Scalar/DCE.h"
#include "llvm/Analysis/TargetLibraryInfo.h"
#include "llvm/Transforms/Scalar.h"
#include "llvm/Transforms/Utils/Local.h"
#include <map>
#include <vector>
#include <algorithm>
using namespace llvm;
using namespace std;

bool isDefinition(Instruction *I) {
	return !(isa<TerminatorInst>(I) || isa<CallInst>(I));
}

static bool countInstructions(Function &F){
	 std::map<std::string, int> opCounter;
	 //errs() << "Function " << F.getName() << '\n';
	 for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
	 	for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; ++i) {
	 		if(opCounter.find(i->getOpcodeName()) == opCounter.end()) {
	 			opCounter[i->getOpcodeName()] = 1;
	 		} else {
	 			opCounter[i->getOpcodeName()] += 1;
	 		}	
	 	}
 	 }
	 std::map <std::string, int>::iterator i = opCounter.begin();
	 std::map <std::string, int>::iterator e = opCounter.end();
	 while (i != e) {
		 errs() << i->first << ": " << i->second << "\n";
		 i++;
	 }
	 //errs() << "\n";
	 opCounter.clear();
	 return false;
}

static bool DCE(Function &F){
	bool deadInstructionFound = false;
	int x = 1;	
	SmallVector<Instruction *, 64> Worklist;
	// in[n] = use[n] U (out[n] - def[n])
	// out[n] = in[s]  ----> s = succ[n]
	std::map<Instruction *, std::vector<Instruction*>> in;  // Variables live on entry to Instruction 
	std::map<Instruction *, std::vector<Instruction*>> out; // Variables live on exit from Instruction
	//std::map<Instruction *, std::vector<Instruction*>> use; // Variables used in the Instruction
	//std::map<Instruction *, std::vector<Instruction*>> def; // Variables defined at Instruction
	std::vector<Instruction*> temp_in; // Will hold the variable live on entry on the succeeding Instruction
	//in = {};
	//out = {};	
	//temp_in = {};
	// Put all instructions in the Worklist
	for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
	 	for (BasicBlock::iterator bi = bb->begin(), be = bb->end(); bi != be; ++bi) {
			Worklist.push_back(&*bi);
		}
		// Consider putting the while loop here to handle multiple blocks in function?
		while (!Worklist.empty()) {
			// Use the last Instruction
			// errs() << "while loop " << x << "\n";
			// errs() << "size of worklist at the start of iteration is  " << Worklist.size() << "\n";
	    		Instruction *I = Worklist.back();
	    		Worklist.pop_back();

			// errs() << "Instruction taken is --> " << I->getOpcodeName() << "\n";
		
			//out[I].clear();
			//in[I].clear();

	    		out[I] = temp_in;    // out[n] = in[s]  ----> s = succ[n] 
			in[I] = out[I];
			temp_in.clear();
			/*
			errs() << "------ out ---------------\n";
			for (std::vector<Instruction*>::const_iterator i = out[I].begin(); i != out[I].end(); ++i)
	    			errs() << *i << "\n";
			errs() << "----------------------------------\n";
			errs() << "------ in ---------------\n";
			for (std::vector<Instruction*>::const_iterator i = in[I].begin(); i != in[I].end(); ++i)
	    			errs() << *i << "\n";
			errs() << "----------------------------------\n";
			errs() << "------ temp_in ---------------\n";
			for (std::vector<Instruction*>::const_iterator i = temp_in.begin(); i != temp_in.end(); ++i)
	    			errs() << *i << "\n";
			errs() << "----------------------------------\n";
			*/
		
			// in set already has the out inside it
			// Filling in the in set with the use now
			// errs() << "Instruction taken is -->>>>>>>>>>>>>>>>>>> " << I->getOpcodeName() << "\n";
			for (User::op_iterator op_i = I->op_begin(), op_e = I->op_end(); op_i != op_e; ++op_i) {
				Value *value = *op_i;
				//errs() << "Instruction taken is --> " << value->getOpcodeName() << "\n";
				if (isa<Instruction>(value) || isa<Argument>(value)) {
					// temp_in.push_back(dyn_cast<Instruction>(*op_i));
					// errs() << "if (isa)-----------\n";
					if (Instruction *ii = dyn_cast<Instruction>(value)) {
		  				in[I].push_back(ii);
						// errs() << "in.push_back( " << ii << " )  --> " << ii->getOpcodeName() << "\n";
					}					
				}
			}
			temp_in = in[I];    // set in to temp_in to be used as in[s] in the next iteration
			/*
			errs() << "------temp_in = in;---------------\n";
			for (std::vector<Instruction*>::const_iterator i = temp_in.begin(); i != temp_in.end(); ++i)
	    			errs() << *i << "\n";
			errs() << "----------------------------------\n";

			//in.clear();
			//out.clear();
			errs() << "size of temp_in:  " << temp_in.size() << "\n";
			*/
			if (temp_in.size() != 0) {
				// errs() << "I just entered the if statement about size of temp_in\n";
				for (User::op_iterator op_i = I->op_begin(), op_e = I->op_end(); op_i != op_e; ++op_i) {
					if (Instruction *used_instr = dyn_cast<Instruction>(*op_i)) {
						// errs() << "Worklist.push_back( " << used_instr << " )\n";
						Worklist.push_back(used_instr);
					}
				}
			}
		
			// the return statement seems to be deleted, so I take care of that in a not conventional way
			if (I->getOpcode() == Instruction::Ret) {
				// do not erase me
			} else {
				if (std::find(out[I].begin(), out[I].end(), I) != out[I].end()) {
					/* out[I] contains the definition of the Instruction */
				} else {
					// errs() << "I will erase ----------->>>>>>>>>> " << I->getOpcodeName() << "\n";		
					I->eraseFromParent();
					Worklist.erase(std::remove(Worklist.begin(), Worklist.end(), I), Worklist.end());
					deadInstructionFound = true;
				}
			}		
			
			// errs() << "size of worklist at the end of iteration is  " << Worklist.size() << "\n\n";
			++x;
	   		
	  	}
	}
	
	
	
	return deadInstructionFound;
}

namespace {
   struct MyDCE : public FunctionPass {
     static char ID;
     MyDCE() : FunctionPass(ID) {}
     
     
     virtual bool runOnFunction(Function &F) {
       errs() << "BEFORE DCE\n";
       //Count before dead code elimination here
       countInstructions(F);
       //Implement your live variable analysis here
       errs() << "DCE START\n";
       DCE(F);
       //Eliminate dead code and produce counts here
       countInstructions(F);
       errs() << "DCE END\n";
       return false;
     }
   };
}
char MyDCE::ID = 0;
static RegisterPass<MyDCE> X("mydce", "My dead code elimination");

