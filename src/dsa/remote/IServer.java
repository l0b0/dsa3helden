package dsa.remote;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import dsa.model.characters.Energy;
import dsa.model.characters.Property;

public interface IServer extends Remote {

	// general interface
	int addClient(String playerName, boolean isGM, int version) throws RemoteException, ServerException;
	void removeClient(int clientId) throws RemoteException, ServerException;
	ArrayList<RemoteUpdate> getUpdates(int clientId) throws RemoteException, ServerException;
	
	// interface for players: upload
	void addHero(int clientId, String name) throws RemoteException, ServerException;
	void removeHero(int clientId, String name) throws RemoteException, ServerException;
	void updateHero(int clientId, String name, String serializedHero) throws RemoteException, ServerException;
	void changeHeroName(int clientId, String oldName, String newName) throws RemoteException, ServerException;
	
	void informGMOfProbe(int clientId, String heroName, String probeResult) throws RemoteException, ServerException;
	void informPlayersOfProbe(int clientId, String heroName, String probeResult) throws RemoteException, ServerException;
	
	void informGMOfRegeneration(int clientId, String heroName, String text, int le, int ae, int ke) throws RemoteException, ServerException;
	void informPlayersOfRegeneration(int clientId, String heroName, String text, int le, int ae, int ke) throws RemoteException, ServerException;
	
	void informGMOfEnergyChange(int clientId, String heroName, Energy energy, int newValue) throws RemoteException, ServerException;
	void informGMOfPropertyChange(int clientId, String heroName, Property property, int newValue) throws RemoteException, ServerException;

	// interface for GM: upload
	void informPlayerOfProbe(String heroName, String probeResult, boolean informAllPlayers) throws RemoteException, ServerException;
	void informPlayerOfRegeneration(String heroName, String text, int le, int ae, int ke, boolean informAllPlayers) throws RemoteException, ServerException;
	void informPlayerOfEnergyChange(String heroName, Energy energy, int newValue) throws RemoteException, ServerException;
	void informPlayerOfPropertyChange(String heroName, Property property, int newValue) throws RemoteException, ServerException;
	
	static final String REGISTERED_NAME = "dsa.HV_Server";
	static final int SERVER_VERSION = 3;
	
	// interface for players: fighting
	enum FightProperty {
		atBonus1,
		atBonus2,
		dazed,
		grounded,
		stumbled,
		markers
	}
	
	void informGMOfAttack(int clientId, String heroName, String text, int quality, boolean hit, int tp, boolean isWeaponLess, boolean informOtherPlayers) 
			throws RemoteException, ServerException;
	void informGMOfProjectileAttack(int clientId, String heroName, String text, boolean hit, int tp, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	void informGMOfParade(int clientId, String heroName, String text, boolean success, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	void informGMOfHit(int clientId, String heroName, String text, int newLe, int newAu, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	void informGMOfFightPropertyChange(int clientId, String heroName, FightProperty fp, int newValue, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	void informGMOfWeaponChange(int clientId, String heroName, String fightMode, String firstHand, String secondHand, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	
	// interface for GM: fighting
	void informPlayerOfHeroAttack(String heroName, String text, boolean informAllPlayers) 
			throws RemoteException, ServerException;
	void informPlayerOfOpponentAttack(String heroName, String opponentName, String text, int quality, boolean hit, int tp, boolean isWeaponLess, 
			boolean informAllPlayers) throws RemoteException, ServerException;
	void informPlayerOfHeroProjectileAttack(String heroName, String text, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	void informPlayerOfOpponentProjectileAttack(String heroName, String opponentName, String text, boolean hit, int tp, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	void informPlayerOfHeroParade(String heroName, String text, boolean success, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	void informPlayerOfOpponentParade(String heroName, String opponentName, String text, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	void informPlayerOfHeroHit(String heroName, String text, int newLe, int newAu, boolean informOtherPlayers)
	        throws RemoteException, ServerException;
	void informPlayerOfFightPropertyChange(String heroName, FightProperty fp, int newValue, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	void informPlayerOfWeaponChange(String heroName, String fightMode, String firstHand, String secondHand, boolean informOtherPlayers)
			throws RemoteException, ServerException;
	
	interface PlayerUpdateVisitor {
		void visitHeroProbe(HeroProbe hp);
		void visitHeroEnergyUpdate(HeroEnergyUpdate heu);
		void visitHeroPropertyUpdate(HeroPropertyUpdate hpu);		
		void visitHeroRegeneration(HeroRegeneration hr);
		
		void visitHeroMeleeAttack(HeroMeleeAttack hma);
		void visitHeroProjectileAttack(HeroProjectileAttack hpa);
		void visitHeroParade(Parade pa);
		void visitHeroHit(HeroHit hh);
		
		void visitOpponentMeleeAttack(OpponentMeleeAttack oma);
		void visitOpponentProjectileAttack(OpponentProjectileAttack opa);
		void visitOpponentParade(Parade pa);
		
		void visitFightPropertyChange(FightPropertyChange fpc);
		void visitWeaponChange(WeaponChange wc);
	}
	
	interface GMUpdateVisitor extends PlayerUpdateVisitor {
		void visitHeroAddition(HeroAddition ha);
		void visitHeroRemoval(HeroRemoval hr);
		void visitHeroUpdate(HeroUpdate hu);
		void visitHeroNameChange(HeroNameChange hnc);
	}
	
	abstract class RemoteUpdate implements Serializable {
		private String heroName;
		
		public String getHeroName() { return heroName; }
		
		public abstract void visitByPlayer(PlayerUpdateVisitor visitor);
		public abstract void visitByGM(GMUpdateVisitor visitor);
		
		protected RemoteUpdate(String heroName) { this.heroName = heroName; } 
	}
	
	class HeroAddition extends RemoteUpdate {
		public HeroAddition(String heroName) {
			super(heroName);
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) {}
		public void visitByGM(GMUpdateVisitor visitor) { visitor.visitHeroAddition(this); }
	}
	
	class HeroRemoval extends RemoteUpdate {
		public HeroRemoval(String heroName) {
			super(heroName);
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) {}
		public void visitByGM(GMUpdateVisitor visitor) { visitor.visitHeroRemoval(this); }
	}
	
	class HeroUpdate extends RemoteUpdate {
		public HeroUpdate(String heroName, String serializedHero) {
			super(heroName);
			this.data = serializedHero;
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) {}
		public void visitByGM(GMUpdateVisitor visitor) { visitor.visitHeroUpdate(this); }
		private String data;
		public String getSerializedHero() { return data; }
	}
	
	class HeroNameChange extends RemoteUpdate {
		public HeroNameChange(String oldName, String newName) {
			super(oldName);
			this.newName = newName;
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) {}
		public void visitByGM(GMUpdateVisitor visitor) { visitor.visitHeroNameChange(this); }
		private String newName;
		public String getNewName() { return newName; }
	}
	
	class HeroProbe extends RemoteUpdate {
		public HeroProbe(String heroName, String result) {
			super(heroName);
			this.result = result;
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitHeroProbe(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitor.visitHeroProbe(this); }
		private String result;
		public String getResult() { return result; }
	}
	
	class HeroEnergyUpdate extends RemoteUpdate {
		public HeroEnergyUpdate(String heroName, Energy energy, int value) {
			super(heroName);
			this.energy = energy;
			this.value = value;
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitHeroEnergyUpdate(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitor.visitHeroEnergyUpdate(this); }
		private Energy energy;
		private int value;
		public Energy getEnergy() { return energy; }
		public int getValue() { return value; }
	}
	
	class HeroRegeneration extends RemoteUpdate {
		public HeroRegeneration(String heroName, String text, int le, int ae, int ke) {
			super(heroName);
			this.le = le;
			this.ae = ae;
			this.ke = ke;
			this.text = text;
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitHeroRegeneration(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitor.visitHeroRegeneration(this); }
		private int le;
		private int ae;
		private int ke;
		private String text;
		public int getLe() { return le; }
		public int getAe() { return ae; }
		public int getKe() { return ke; }
		public String getText() { return text; }
	}
	
	class HeroPropertyUpdate extends RemoteUpdate {
		public HeroPropertyUpdate(String heroName, Property property, int value) {
			super(heroName);
			this.property = property;
			this.value = value;
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitHeroPropertyUpdate(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitor.visitHeroPropertyUpdate(this); }
		private Property property;
		private int value;
		public Property getProperty() { return property; }
		public int getValue() { return value; }		
	}
	
	class HeroHit extends RemoteUpdate {
		public HeroHit(String heroName, String text, int newLe, int newAu) {
			super(heroName);
			this.le = newLe;
			this.au = newAu;
			this.text = text;
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitHeroHit(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitor.visitHeroHit(this); }
		private int le;
		private int au;
		private String text;
		public int getLe() { return le; }
		public int getAu() { return au; }
		public String getText() { return text; }
	}

	abstract class Attack extends RemoteUpdate {
		protected Attack(String heroName, String text, boolean hit, int tp) {
			super(heroName);
			this.text = text;
			this.hit = hit;
			this.tp = tp;
		}
		
		public String getText() { return text; }
		public boolean wasHit() { return hit; }
		public int getTP() { return tp; }

		private String text;
		private boolean hit;
		private int tp;
	}
	
	abstract class MeleeAttack extends Attack {
		protected MeleeAttack(String heroName, String text, int quality, boolean hit, int tp, boolean isWeaponLess) {
			super(heroName, text, hit, tp);
			this.quality = quality;
			this.isWeaponLess = isWeaponLess;
		}
		
		public int getQuality() { return quality; }
		public boolean isWeaponLess() { return isWeaponLess; }
		
		private int quality;
		private boolean isWeaponLess;
	}
	
	class HeroMeleeAttack extends MeleeAttack {
		public HeroMeleeAttack(String heroName, String text, int quality, boolean hit, int tp, boolean isWeaponLess) {
			super(heroName, text, quality, hit, tp, isWeaponLess);
		}
		
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitHeroMeleeAttack(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitByPlayer(visitor); }
	}

	class OpponentMeleeAttack extends MeleeAttack {
		public OpponentMeleeAttack(String opponentName, String heroName, String text, int quality, boolean hit, int tp, boolean isWeaponLess) {
			super(heroName, text, quality, hit, tp, isWeaponLess);
			this.opponentName = opponentName;
		}
		
		public String getOpponentName() { return opponentName; }
		
		private String opponentName;
		
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitOpponentMeleeAttack(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitByPlayer(visitor); }
	}
	
	abstract class ProjectileAttack extends Attack {
		protected ProjectileAttack(String heroName, String text, boolean hit, int tp) {
			super(heroName, text, hit, tp);
		}
	}
	
	class HeroProjectileAttack extends ProjectileAttack {
		public HeroProjectileAttack(String heroName, String text, boolean hit, int tp) {
			super(heroName, text, hit, tp);
		}
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitHeroProjectileAttack(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitByPlayer(visitor); }
	}
	
	class OpponentProjectileAttack extends ProjectileAttack {
		public OpponentProjectileAttack(String opponentName, String heroName, String text, boolean hit, int tp) {
			super(heroName, text, hit, tp);
			this.opponentName = opponentName;
		}
		
		public String getOpponentName() { return opponentName; }
		
		private String opponentName;
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitOpponentProjectileAttack(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitByPlayer(visitor); }
	}
	
	
	class Parade extends RemoteUpdate {
		public Parade(String heroOrOpponentName, String text, boolean success, boolean isHero) {
			super(heroOrOpponentName);
			this.text = text;
			this.success = success;
			this.isHero = isHero;
		}
		
		public String getText() { return text; }
		
		public boolean wasSuccessful() { return success; }
		
		private String text;
		private boolean success;
		private boolean isHero;

		public void visitByPlayer(PlayerUpdateVisitor visitor) { if (isHero) visitor.visitHeroParade(this); else visitor.visitOpponentParade(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitByPlayer(visitor); }
	}
	
	class FightPropertyChange extends RemoteUpdate {
		public FightPropertyChange(String heroName, FightProperty fp, int newValue) {
			super(heroName);
			this.fp = fp;
			value = newValue;
		}
		
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitFightPropertyChange(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitByPlayer(visitor); }
		
		public FightProperty getFightProperty() { return fp; }
		public int getNewValue() { return value; }
		
		private FightProperty fp;
		private int value;
	}
	
	class WeaponChange extends RemoteUpdate {
		public WeaponChange(String heroName, String fightMode, String firstHand, String secondHand) {
			super(heroName);
			this.fightMode = fightMode != null ? fightMode : "";
			this.firstHand = firstHand != null ? firstHand : "";
			this.secondHand = secondHand != null ? secondHand : "";
		}
		
		public String getFightMode() { return fightMode; }
		public String getFirstHand() { return firstHand; }
		public String getSecondHand() { return secondHand; }
		
		public void visitByPlayer(PlayerUpdateVisitor visitor) { visitor.visitWeaponChange(this); }
		public void visitByGM(GMUpdateVisitor visitor) { visitByPlayer(visitor); }
		
		private String fightMode;
		private String firstHand;
		private String secondHand;
	}

}

