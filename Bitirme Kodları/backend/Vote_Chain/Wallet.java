import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
public class Wallet {
	
	public PrivateKey privateKey;
	public PublicKey publicKey;
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //only UTXOs owned by this wallet
	public Wallet(){
		generateKeyPair();	
	}
		
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			//256 bytes provides an acceptable security level
			keyGen.initialize(ecSpec, random);   
	        	KeyPair keyPair = keyGen.generateKeyPair();
	        	// Set the public and private keys from the keyPair
	        	privateKey = keyPair.getPrivate();
	        	publicKey = keyPair.getPublic();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	public float getBalance() {
		float total = 0;	
        for (Map.Entry<String, TransactionOutput> item: VoteChainTest.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
            	UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
            	total += UTXO.value ; 
            }
        }  
		return total;
	}
	//Generates and returns a new transaction from this wallet.
	public Transaction sendFunds(PublicKey _recipient,float value ) {
		if(getBalance() < value) { //gather balance and check funds.
			System.out.println("Uygunsuz oy kullanımı deneniyor... işlem iptal edildi !!!");
			return null;
		}
    //create array list of inputs
	ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

	float total = 0;
	for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
		TransactionOutput UTXO = item.getValue();
		total += UTXO.value;
		inputs.add(new TransactionInput(UTXO.id));
		if(total > value) break;
	}
	
	Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
	newTransaction.generateSignature(privateKey);
	
	for(TransactionInput input: inputs){
		UTXOs.remove(input.transactionOutputId);
	}
	return newTransaction;
	}	
}