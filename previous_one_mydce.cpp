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
	SmallVector<Instruction *, 64> Worklist;
	// in[n] = use[n] U (out[n] - def[n])
	// out[n] = in[s]  ----> s = succ[n]
	std::vector<Instruction*> in;  // Variables live on entry to Instruction 
	std::vector<Instruction*> out; // Variables live on exit from Instruction
	std::vector<Instruction*> use; // Variables used in the Instruction
	std::vector<Instruction*> def; // Variables defined at Instruction
	std::vector<Instruction*> temp_in; // will hold the variable live on entry on the succeeding Instruction
	in = {};
	out = {};	
	temp_in = {};
	// Aaron said opposite approach
	// Put all instructions in the Worklist
	for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
	 	for (BasicBlock::iterator bi = bb->begin(), be = bb->end(); bi != be; ++bi) {
			Worklist.push_back(&*bi);
		}
	}
	
	while (!Worklist.empty()) {
		// Use the last Instruction
		errs() << "while loop\n";
    		Instruction *I = Worklist.back();
    		Worklist.pop_back();
		
    		out = temp_in;    // out[n] = in[s]  ----> s = succ[n] 
		in = out;

		for (std::vector<Instruction*>::const_iterator i = out.begin(); i != out.end(); ++i)
    			errs() << "in  >>> " << *i << "\n";

		if (!isDefinition(I) || I->mayHaveSideEffects()) {
			errs() << "in !isDefinition\n";
			// Iterate on the variables and push the used Instructions
      			for (User::op_iterator op_i = I->op_begin(), op_e = I->op_end(); op_i != op_e; ++op_i) {
				Value *value = *op_i;
				if (isa<Instruction>(value) || isa<Argument>(value)) {
					//temp_in.push_back(dyn_cast<Instruction>(*op_i));
					errs() << "this is added in vector IN " << op_i << "\n";
					if (Instruction *ii = dyn_cast<Instruction>(value)) {
		  				in.push_back(ii);
					}					
				}
			}
			temp_in = in;    // set in to temp_in to be used as in[s] in the next iteration
		
			for (std::vector<Instruction*>::const_iterator i = temp_in.begin(); i != temp_in.end(); ++i)
	    			errs() << ":" << *i << "\n";

			in.clear();
			out.clear();
			errs() << "size of temp_in:  " << temp_in.size() << "\n";
			if (temp_in.size() != 0) {
				errs() << "I just entered the if statement about size of temp_in\n";
				for (User::op_iterator op_i = I->op_begin(), op_e = I->op_end(); op_i != op_e; ++op_i) {
					if (Instruction *used_instr = dyn_cast<Instruction>(*op_i)) {
			  			Worklist.push_back(used_instr);
					}
				}
			}
			// Remove and erase the Instruction
	      		I->eraseFromParent();
	      		Worklist.erase(std::remove(Worklist.begin(), Worklist.end(), I), Worklist.end());

	      		deadInstructionFound = true;
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
       DCE(F);
       errs() << "DCE START\n";
       //Eliminate dead code and produce counts here
       countInstructions(F);
       errs() << "DCE END\n";
       return false;
     }
   };
}
char MyDCE::ID = 0;
static RegisterPass<MyDCE> X("mydce", "My dead code elimination");

