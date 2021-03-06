/*
 Copyright (c) 2006-2008 [Joerg Ruedenauer]
 
 This file is part of Heldenverwaltung.

 Heldenverwaltung is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Heldenverwaltung is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Heldenverwaltung; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.model.characters;

import java.io.File;
import java.io.IOException;
import java.util.List;

import dsa.model.Date;
import dsa.model.Fighter;
import dsa.model.ThingCarrier;
import dsa.model.data.Adventure;
import dsa.model.data.Animal;

/**
 * 
 */
public interface Hero extends Printable, Fighter, ThingCarrier {
  
  long CLOTHES = 1;
  long THINGS = 2;
  long ARMOURS = 4;
  long WEAPONS = 8;
  long SHIELDS = 16;
  long WAREHOUSE = 32;

  int getDefaultProperty(Property property);

  int getCurrentProperty(Property property);

  void setProperty(int index, int newValue);

  int getDefaultEnergy(Energy energy);

  int getCurrentEnergy(Energy energy);

  boolean hasEnergy(Energy energy);

  void setDefaultProperty(Property property, int value);

  void setCurrentProperty(Property property, int value);

  void setDefaultEnergy(Energy energy, int value);

  void setCurrentEnergy(Energy energy, int value);

  void changeDefaultProperty(Property property, int mod);

  void changeCurrentProperty(Property property, int mod);

  void changeDefaultEnergy(Energy energy, int mod);

  void changeCurrentEnergy(Energy energy, int mod);

  void setHasEnergy(Energy energy, boolean hasIt);

  int getMRBonus();

  int getAP();

  int getStep();

  void setAP(int ap);

  void changeAP(int mod);
  
  int getRemainingStepIncreases();

  boolean hasTalent(String talent);

  int getDefaultTalentValue(String talent);

  int getCurrentTalentValue(String talent);

  void setDefaultTalentValue(String talent, int value);

  void setCurrentTalentValue(String talent, int value);

  void changeTalentValue(String talent, int mod);

  int getOverallTalentIncreaseTries();

  int getOverallSpellIncreaseTries();

  int getTalentIncreaseTries(String talent);

  int getTalentIncreaseTriesPerStep(String talent);

  void setTalentIncreaseTriesPerStep(String talent, int incrTries);

  int getSpellOrTalentIncreaseTries();

  boolean hasGreatMeditation();

  int doGreatMeditation();

  void removeRemainingIncreaseTries();

  boolean hasPropertyChangeTry(Property property);

  void removePropertyChangeTry(boolean goodProperty);

  boolean hasLEIncreaseTry();

  boolean hasAEIncreaseTry();

  boolean hasExtraAEIncrease();

  void increaseLEAndAE(int lePlus, int aePlus) throws LEAEIncreaseException;

  void increaseLE(int lePlus);

  void increaseAE(int aePlus);

  int getFixedLEIncrease();

  int getFixedAEIncrease();

  String getType();

  String getBirthPlace();

  String getEyeColor();

  String getHairColor();

  String getSkinColor();

  String getHeight();

  String getWeight();

  String getSex();

  Date getBirthday();

  String getStand();

  String getGod();

  int getAge();

  String getTitle();

  void setBirthPlace(String birthplace);

  void setEyeColor(String eyeColor);

  void setHairColor(String hairColor);

  void setSkinColor(String skinColor);

  void setHeight(String height);

  void setWeight(String weight);

  void setSex(String sex);

  void setBirthday(Date birthday);

  void setStand(String stand);

  void setGod(String god);

  void setAge(int age);

  void setTitle(String title);

  void setName(String newName);

  void removeTalentIncreaseTry(String talent, boolean succeded);

  void addHeroObserver(CharacterObserver observer);

  void removeHeroObserver(CharacterObserver observer);

  void storeToFile(java.io.File file, File realFile) throws java.io.IOException;
  String storeToString();

  boolean isChanged();

  String getRuf();

  void addTalent(String name);

  void removeTalent(String name);

  enum DerivedValue {
    AT, PA, FK, AW, AB, TK, MR
  }

  int getCurrentDerivedValue(DerivedValue dv);

  int getDefaultDerivedValue(DerivedValue dv);

  int getCurrentDerivedValueChange(DerivedValue dv);

  void setCurrentDerivedValueChange(DerivedValue dv, int change);

  int getNrOfCurrencies(boolean bank);

  int getMoney(int currencyIndex, boolean bank);

  void setMoney(int currencyIndex, int value, boolean bank);

  void addCurrency(boolean bank);

  void removeCurrency(boolean bank);

  void setCurrency(int currencyIndex, int currency, boolean bank);

  int getCurrency(int currencyIndex, boolean bank);

  int getFreeLanguagePoints();

  int getFreeOldLanguagePoints();

  String getNativeTongue();

  void setNativeTongue(String tongue);

  String[] getArmours();

  void addArmour(String name);

  void removeArmour(String name);

  int getBE();

  int getRS();

  int getBEModification();

  String[] getWeapons();

  String addWeapon(String name);

  void removeWeapon(String name);

  int getWeaponCount(String name);

  String[] getShields();

  void addShield(String name);

  void removeShield(String name);

  String getBGFile();

  String getBGEditor();

  String getNotes();

  void setBGFile(String name);

  void setBGEditor(String name);

  void setNotes(String notes);

  void setPicture(String location);

  String getPictureLocation();

  int getATPart(String talent);

  int getPAPart(String talent);

  void setATPart(String talent, int value);

  void setPAPart(String talent, int value);

  String getSkin();

  void setSkin(String skin);

  List<String> getRituals();

  void addRitual(String ritual);

  void removeRitual(String ritual);

  int getNrOfAnimals();

  Animal getAnimal(int index);

  void addAnimal(Animal animal);

  void removeAnimal(int index);

  String getElement();

  void setElement(String element);

  String getAcademy();

  void setAcademy(String academy);

  String getSpecialization();

  void setSpecialization(String specialization);

  String getSoulAnimal();

  void setSoulAnimal(String animal);

  void addClothes(String item);

  void removeClothes(String item);

  String[] getClothes();

  void toStepOne(int talentReducement);

  int getTalentIncreasesPerStep();

  int getSpellIncreasesPerStep();

  int getSpellToTalentMoves();

  boolean canDoGreatMeditation();

  void setCanDoGreatMeditation(boolean can);

  void setHasExtraAEIncrease(boolean extra);

  void setMRBonus(int bonus);

  void setBEModification(int mod);

  void setTalentIncreasesPerStep(int increases);

  void setSpellIncreasesPerStep(int increases);

  void setSpellToTalentMoves(int moves);

  void setFixedLEIncrease(int increase);

  void setFixedAEIncrease(int increase);

  void setType(String type);

  String getFightMode();

  void setFightMode(String mode);

  String getFirstHandWeapon();

  void setFirstHandWeapon(String name);

  String getSecondHandItem();

  void setSecondHandItem(String name);

  int getAT1Bonus();

  void setAT1Bonus(int bonus);

  int getAT2Bonus();

  void setAT2Bonus(int bonus);

  int getBF(String weaponName, int weaponNr);

  void setBF(String weaponName, int weaponNr, int bf);

  int getBF(String shieldName);

  void setBF(String shieldName, int bf);

  int getExtraMarkers();

  void setExtraMarkers(int markers);

  boolean fightUsesAU();

  void setFightUsesAU(boolean useAU);

  void changeAU(int difference);
  
  void setNrOfProjectiles(String weaponName, int nrOfProjectiles);
  
  int getNrOfProjectiles(String weaponName);
  
  String getInternalType();
  
  void setInternalType(String typeName);
  
  boolean hasLoadedNewerVersion();
  
  void setHasLoadedNewerVersion(boolean newer);
  
  void storeThingsToFile(File f) throws IOException;
  
  void readThingsFromFile(long thingTypes, File f) throws IOException;
  
  void fireActiveWeaponsChanged();

  boolean isMagicDilletant();

  void setIsMagicDilettant(boolean dt);
  
  void addAdventure(Adventure adventure);
  
  void removeAdventure(int index);
  
  void moveAdventureUp(int index);
  
  void moveAdventureDown(int index);
  
  Adventure[] getAdventures();
  
  int getSO();
  
  void setSO(int so);
  
  void setStepDifference(int difference);
  
  boolean canPay(int price, dsa.model.data.Thing.Currency currency);
  
  String getKnownNPCs();
  void setKnownNPCs(String text);
  
  String getKnownPCs();
  void setKnownPCs(String text);
}
