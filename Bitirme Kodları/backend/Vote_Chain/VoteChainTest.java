import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.Base64;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
public class VoteChainTest {
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public static int difficulty = 1;
	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;

	public static void main(String[] args) {	
		//add our blocks to the blockchain ArrayList:
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
		int enterWallet=0;
		try{
			FileReader fileReader = new FileReader("login.txt");
			String line;

			BufferedReader br = new BufferedReader(fileReader);
			Scanner input = new Scanner(System.in);
			System.out.println("Elinizi el okuma cihazına yerleştirerek, oy kullanımı aktif ediniz");
			
			String privateKey=input.next();
			while ((line = br.readLine()) != null) {

			    if(line.equals(privateKey)){
			    	++enterWallet;
			    }
			}

			br.close();	

	    if(enterWallet==1){
	    	System.out.println("Kimlik doğrulama tamamlandı. Oy kullanabilirsiniz...");
	    	for (int i =0; i<1000000000; ++i); 
			//Create wallets:
	    	System.out.println("Seçmen cüzdanı oluşturuluyor...");
	    	for ( int j =0; j<100000000; ++j); 
			walletA = new Wallet();
			walletB = new Wallet();		
			Wallet coinbase = new Wallet();
			
	    	System.out.println("Seçmen cüzdanına token transfer ediliyor...");
	    	for (int k =0; k<1000000000; ++k); 
			genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
			genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction	
			genesisTransaction.transactionId = "0"; //manually set the transaction id
			genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
			UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
			
			System.out.println("Oyunuzu kullanınız...");
		    input = new Scanner(System.in);
			String oy=input.next();
			System.out.println("Oy gönderme için ilk blok(genesis) ve mining başlatılıyor.. ");
			Block genesis = new Block("0");
			genesis.addTransaction(genesisTransaction);
			addBlock(genesis);
			
			//testing
			Block block1 = new Block(genesis.hash);
			System.out.println("\nCüzdan A miktarı: " + walletA.getBalance());
			System.out.println("\nCüzdan A, Cüzdan B'ye 40 adet token gönderiyor...");
			block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
			addBlock(block1);
			System.out.println("\nCüzdan A miktarı: " + walletA.getBalance());
			System.out.println("Cüzdan B miktarı: " + walletB.getBalance());
			
			System.out.println("Tekrar Oy Kullanmak İster Misiniz?");
			input = new Scanner(System.in);
			String oy2=input.next();
			Block block2 = new Block(block1.hash);
			System.out.println("\nCüzdan A 1000 adet token göndermeyi deniyor...");
			block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
			addBlock(block2);
			System.out.println("\nWalletA's balance is: " + walletA.getBalance());
			System.out.println("WalletB's balance is: " + walletB.getBalance());
			System.out.println("Cüzdan A çıkış yaptı...");

			System.out.println("Cüzdan B giriş yapıyor...");
		    input = new Scanner(System.in);
			String oy3=input.next();
			Block block3 = new Block(block2.hash);
			System.out.println("\nCüzdan B, Cüzdan A'ya 20 adet token gönderimi yaptı...");
			block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
			System.out.println("\nCüzdan A miktarı: " + walletA.getBalance());
			System.out.println("Cüzdan B miktarı: " + walletB.getBalance());
			
			isChainValid();
			
		}
		else{
			System.out.println("Kimlik tanımlanamadı !");
		}
	}
	catch (IOException e) {
            System.out.println("File I/O error!");
    }
	}
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifiySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
}
