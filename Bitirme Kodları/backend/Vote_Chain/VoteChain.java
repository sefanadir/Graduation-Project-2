
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
public class VoteChain {

	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); 
	public static int difficulty = 0;
	public static Wallet walletA;
	public static Wallet walletB;
	public static void main(String[] args) {
		
		Block genesisBlock 	= new Block("Hi im the first block"+ "0");
		System.out.println("Hash for block 1 : " + genesisBlock.hash);
		
		Block secondBlock 	= new Block("Yo im the second block"+genesisBlock.hash);
		System.out.println("Hash for block 2 : " + secondBlock.hash);
		
		Block thirdBlock 	= new Block("Hey im the third block"+secondBlock.hash);
		System.out.println("Hash for block 3 : " + thirdBlock.hash);

		//add our blocks to the blockchain ArrayList:
		blockchain.add(new Block("Hi im the first block"+"0"));		
		blockchain.add(new Block("Yo im the second block"+blockchain.get(blockchain.size()-1).hash)); 
		blockchain.add(new Block("Hey im the third block"+blockchain.get(blockchain.size()-1).hash));
		
		for (int i=0;i<blockchain.size();i++ ) {
			System.out.println("Hash: "			+blockchain.get(i).hash);
			System.out.println("PreviousHash: "	+blockchain.get(i).previousHash);
			System.out.println("Time: "			+blockchain.get(i).timeStamp);
		}

		blockchain.clear();
		System.out.println("VOTING.....");
		
        try {
			blockchain.add(new Block("0"+"Hi im the first block"));
			System.out.println("Trying to Mine block for Vote... ");
			TimeUnit.SECONDS.sleep(2);
			System.out.println("Voting Success... Block mined...\n");
			blockchain.get(0).mineBlock(difficulty);
			
			blockchain.add(new Block(blockchain.get(blockchain.size()-1).hash + "Yo im the second block"));
			System.out.println("Trying to Mine block for Vote... ");
			TimeUnit.SECONDS.sleep(3);
			System.out.println("Voting Success... Block mined...\n");
			blockchain.get(1).mineBlock(difficulty);
			
			blockchain.add(new Block(blockchain.get(blockchain.size()-1).hash + "Hey im the third block"));
			System.out.println("Trying to Mine block for Vote... ");
			TimeUnit.SECONDS.sleep(3);
			System.out.println("Voting Success... Block mined...\n");
			blockchain.get(2).mineBlock(difficulty);	

        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
	
		System.out.println("\nBlockchain is Valid: " + isChainValid());
		
		for (int i=0;i<blockchain.size();i++ ) {
			System.out.println("Hash: "			+blockchain.get(i).hash);
			System.out.println("PreviousHash: "	+blockchain.get(i).previousHash);
			System.out.println("Time: "			+blockchain.get(i).timeStamp);
		}
		System.out.println("WALLET TEST...");
		//Setup Bouncey castle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		//Create the new wallets
		walletA = new Wallet();
		walletB = new Wallet();
		//Test public and private keys
		System.out.println("Private and public keys:");
		System.out.println(DigitalSignature.getStringFromKey(walletA.privateKey));
		System.out.println(DigitalSignature.getStringFromKey(walletA.publicKey));
		//Create a test transaction from WalletA to walletB 
		Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
		transaction.generateSignature(walletA.privateKey);
		//Verify the signature works and verify it from the public key
		System.out.println("Is signature verified");
		System.out.println(transaction.verifiySignature());
	}

	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes not equal");			
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
		}
		return true;
	}
}