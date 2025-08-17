-- MySQL dump 10.13  Distrib 8.0.42, for Linux (x86_64)
--
-- Host: localhost    Database: stellarvision
-- ------------------------------------------------------
-- Server version       8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `collections`
--

DROP TABLE IF EXISTS `collections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collections` (
  `id` int NOT NULL AUTO_INCREMENT,
  `collection_count` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `modified_at` datetime(6) NOT NULL,
  `abbreviation` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `korean_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_COLLECTION_ABBR` (`abbreviation`)
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collections`
--

LOCK TABLES `collections` WRITE;
/*!40000 ALTER TABLE `collections` DISABLE KEYS */;
INSERT INTO `collections` VALUES (1,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','And','안드로메다자리','Andromeda'),(2,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Ant','공기펌프자리','Antlia'),(3,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Aps','극락조자리','Apus'),(4,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Aqr','물병자리','Aquarius'),(5,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Aql','독수리자리','Aquila'),(6,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Ara','제단자리','Ara'),(7,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Ari','양자리','Aries'),(8,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Aur','마차부자리','Auriga'),(9,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Boo','목동자리','Bootes'),(10,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cae','조각칼자리','Caelum'),(11,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cam','기린자리','Camelopardalis'),(12,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cnc','게자리','Cancer'),(13,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','CVn','사냥개자리','Canes Venatici'),(14,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','CMa','큰개자리','Canis Major'),(15,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','CMi','작은개자리','Canis Minor'),(16,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cap','염소자리','Capricornus'),(17,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Car','용골자리','Carina'),(18,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cas','카시오페이아자리','Cassiopeia'),(19,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cen','센타우루스자리','Centaurus'),(20,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cep','세페우스자리','Cepheus'),(21,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cet','고래자리','Cetus'),(22,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cha','카멜레온자리','Chamaeleon'),(23,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cir','컴퍼스자리','Circinus'),(24,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Col','비둘기자리','Columba'),(25,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Com','머리털자리','Coma Berenices'),(26,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','CrA','남쪽왕관자리','Corona Australis'),(27,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','CrB','북쪽왕관자리','Corona Borealis'),(28,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Crv','까마귀자리','Corvus'),(29,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Crt','컵자리','Crater'),(30,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cru','남십자자리','Crux'),(31,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Cyg','백조자리','Cygnus'),(32,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Del','돌고래자리','Delphinus'),(33,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Dor','황새치자리','Dorado'),(34,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Dra','용자리','Draco'),(35,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Equ','조랑말자리','Equuleus'),(36,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Eri','에리다누스자리','Eridanus'),(37,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','For','화로자리','Fornax'),(38,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Gem','쌍둥이자리','Gemini'),(39,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Gru','두루미자리','Grus'),(40,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Her','헤라클레스자리','Hercules'),(41,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Hor','시계자리','Horologium'),(42,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Hya','바다뱀자리','Hydra'),(43,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Hyi','물뱀자리','Hydrus'),(44,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Ind','인디언자리','Indus'),(45,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Lac','도마뱀자리','Lacerta'),(46,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Leo','사자자리','Leo'),(47,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','LMi','작은사자자리','Leo Minor'),(48,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Lep','토끼자리','Lepus'),(49,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Lib','천칭자리','Libra'),(50,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Lup','이리자리','Lupus'),(51,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Lyn','살쾡이자리','Lynx'),(52,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Lyr','거문고자리','Lyra'),(53,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Men','테이블산자리','Mensa'),(54,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Mic','현미경자리','Microscopium'),(55,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Mon','외뿔소자리','Monoceros'),(56,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Mus','파리자리','Musca'),(57,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Nor','직각자자리','Norma'),(58,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Oct','팔분의자리','Octans'),(59,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Oph','뱀주인자리','Ophiuchus'),(60,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Ori','오리온자리','Orion'),(61,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Pav','공작자리','Pavo'),(62,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Peg','페가수스자리','Pegasus'),(63,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Per','페르세우스자리','Perseus'),(64,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Phe','봉황자리','Phoenix'),(65,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Pic','화가자리','Pictor'),(66,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Psc','물고기자리','Pisces'),(67,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','PsA','남쪽물고기자리','Piscis Austrinus'),(68,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Pup','고물자리','Puppis'),(69,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Pyx','나침반자리','Pyxis'),(70,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Ret','그물자리','Reticulum'),(71,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Sge','화살자리','Sagitta'),(72,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Sgr','궁수자리','Sagittarius'),(73,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Sco','전갈자리','Scorpius'),(74,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Scl','조각가자리','Sculptor'),(75,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Sct','방패자리','Scutum'),(76,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Ser','뱀자리','Serpens'),(77,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Sex','육분의자리','Sextans'),(78,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Tau','황소자리','Taurus'),(79,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Tel','망원경자리','Telescopium'),(80,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Tri','삼각형자리','Triangulum'),(81,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','TrA','남쪽삼각형자리','Triangulum Australe'),(82,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Tuc','큰부리새자리','Tucana'),(83,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','UMa','큰곰자리','Ursa Major'),(84,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','UMi','작은곰자리','Ursa Minor'),(85,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Vel','돛자리','Vela'),(86,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Vir','처녀자리','Virgo'),(87,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Vol','날치자리','Volans'),(88,0,'2025-08-07 17:29:58.000000','2025-08-07 17:29:58.000000','Vul','여우자리','Vulpecula');
/*!40000 ALTER TABLE `collections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `follows`
--

DROP TABLE IF EXISTS `follows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `follows` (
  `created_at` datetime(6) NOT NULL,
  `from_member_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `modified_at` datetime(6) NOT NULL,
  `to_member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_FOLLOW_FROM_TO` (`from_member_id`,`to_member_id`),
  KEY `FKj5pnll2hbesc985utafw2ty7w` (`to_member_id`),
  CONSTRAINT `FKcx1obn1hi2phkj64am6ib535u` FOREIGN KEY (`from_member_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKj5pnll2hbesc985utafw2ty7w` FOREIGN KEY (`to_member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;



--
-- Table structure for table `members`
--

DROP TABLE IF EXISTS `members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `members` (
  `birth` date NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `follower_count` bigint NOT NULL,
  `following_count` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `latest_login` datetime(6) DEFAULT NULL,
  `modified_at` datetime(6) NOT NULL,
  `profile_id` bigint DEFAULT NULL,
  `name` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(225) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider` enum('GOOGLE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_MEMBER_EMAIL` (`email`),
  UNIQUE KEY `UKea8frl3f8edbxc47rlw1q6jn5` (`profile_id`),
  CONSTRAINT `FKsegndvtv85mca8225u04pfx4g` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `members_collections`
--

DROP TABLE IF EXISTS `members_collections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `members_collections` (
  `collection_id` int NOT NULL,
  `is_select` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `modified_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo6x4tva72xglj5msp7ni7hjt2` (`collection_id`),
  KEY `FK3hwhr9j8lp2nqww5sunyvt95p` (`member_id`),
  CONSTRAINT `FK3hwhr9j8lp2nqww5sunyvt95p` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKo6x4tva72xglj5msp7ni7hjt2` FOREIGN KEY (`collection_id`) REFERENCES `collections` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `photo_tags`
--

DROP TABLE IF EXISTS `photo_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_tags` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `modified_at` datetime(6) NOT NULL,
  `photo_id` bigint NOT NULL,
  `tag_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKosc6hwdgbuo9c35svqvwnpffj` (`photo_id`),
  CONSTRAINT `FKosc6hwdgbuo9c35svqvwnpffj` FOREIGN KEY (`photo_id`) REFERENCES `photos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=489 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `photos`
--

DROP TABLE IF EXISTS `photos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photos` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `modified_at` datetime(6) NOT NULL,
  `title` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_extension` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `photo_s3_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_PHOTO_S3_KEY` (`photo_s3_key`),
  KEY `FK61ogpnl4cfrqebg7bqdi2vtjf` (`member_id`),
  CONSTRAINT `FK61ogpnl4cfrqebg7bqdi2vtjf` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=383 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `profiles`
--

DROP TABLE IF EXISTS `profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `profiles` (
  `is_collection_public` bit(1) NOT NULL,
  `is_gallery_public` bit(1) NOT NULL,
  `is_video_public` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `modified_at` datetime(6) NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_s3_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_PROFILE_KEY` (`profile_s3_key`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `created_at` datetime(6) NOT NULL,
  `from_member_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `modified_at` datetime(6) NOT NULL,
  `report_category_id` bigint NOT NULL,
  `to_member_id` bigint NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `FKihqj9t3fys2r34gjv3hfcd03h` (`from_member_id`),
  KEY `FKe7qvx41kbod2cnuswur6teob2` (`report_category_id`),
  KEY `FKorysa7ghluafui1j4oeqht5mp` (`to_member_id`),
  CONSTRAINT `FKe7qvx41kbod2cnuswur6teob2` FOREIGN KEY (`report_category_id`) REFERENCES `reports_categories` (`id`),
  CONSTRAINT `FKihqj9t3fys2r34gjv3hfcd03h` FOREIGN KEY (`from_member_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKorysa7ghluafui1j4oeqht5mp` FOREIGN KEY (`to_member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports_categories`
--

DROP TABLE IF EXISTS `reports_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports_categories` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `modified_at` datetime(6) NOT NULL,
  `category` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports_categories`
--

LOCK TABLES `reports_categories` WRITE;
/*!40000 ALTER TABLE `reports_categories` DISABLE KEYS */;
/*!40000 ALTER TABLE `reports_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `streaming_rooms`
--

DROP TABLE IF EXISTS `streaming_rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `streaming_rooms` (
  `is_deleted` bit(1) NOT NULL,
  `latitude` decimal(9,6) NOT NULL,
  `longitude` decimal(9,6) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `modified_at` datetime(6) NOT NULL,
  `owner_member_id` bigint NOT NULL,
  `recorded_at` datetime(6) DEFAULT NULL,
  `recording_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recording_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `session_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_SESSION_ID` (`session_id`),
  UNIQUE KEY `UK_RECORDING_ID` (`recording_id`),
  KEY `FK5wr93rtgwl94n2hwddhadg862` (`owner_member_id`),
  CONSTRAINT `FK5wr93rtgwl94n2hwddhadg862` FOREIGN KEY (`owner_member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=541 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `thumbnails`
--

DROP TABLE IF EXISTS `thumbnails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thumbnails` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `modified_at` datetime(6) NOT NULL,
  `thumbnail_s3_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_THUMBNAIL_S3_KEY` (`thumbnail_s3_key`)
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thumbnails`
--

--
-- Table structure for table `video_likes`
--

DROP TABLE IF EXISTS `video_likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video_likes` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `modified_at` datetime(6) NOT NULL,
  `video_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_MEMBER_VIDEO` (`member_id`,`video_id`),
  KEY `FKh5xdmkusmdp62bin35eulq2ny` (`video_id`),
  CONSTRAINT `FKbr8abvw39og7xtf67obtk1cvs` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKh5xdmkusmdp62bin35eulq2ny` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=154 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `video_tags`
--

DROP TABLE IF EXISTS `video_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video_tags` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `modified_at` datetime(6) NOT NULL,
  `video_id` bigint NOT NULL,
  `tag_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_video_tag` (`video_id`,`tag_name`),
  CONSTRAINT `FKpr6ks7ia3ilx9ec2mmwb82lb6` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `videos`
--

DROP TABLE IF EXISTS `videos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `videos` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `like_count` bigint DEFAULT '0',
  `member_id` bigint NOT NULL,
  `modified_at` datetime(6) NOT NULL,
  `thumbnail_id` bigint NOT NULL,
  `title` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `video_s3_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_VIDEO_S3_KEY` (`video_s3_key`),
  KEY `FKb76wwhxk2vxqc86whci44ocop` (`member_id`),
  KEY `FK9cne6ydfiek9062h34dmd7hir` (`thumbnail_id`),
  CONSTRAINT `FK9cne6ydfiek9062h34dmd7hir` FOREIGN KEY (`thumbnail_id`) REFERENCES `thumbnails` (`id`),
  CONSTRAINT `FKb76wwhxk2vxqc86whci44ocop` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videos`
--

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;