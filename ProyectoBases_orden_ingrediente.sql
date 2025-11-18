CREATE DATABASE  IF NOT EXISTS `ProyectoBases` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `ProyectoBases`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 192.168.0.10    Database: ProyectoBases
-- ------------------------------------------------------
-- Server version	5.7.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `orden_ingrediente`
--

DROP TABLE IF EXISTS `orden_ingrediente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orden_ingrediente` (
  `orden_id` int(11) NOT NULL,
  `codigo_ingrediente` int(11) NOT NULL,
  KEY `FKsjpu6m25y2h2yleytjkp4igo3` (`codigo_ingrediente`),
  KEY `FKcmv8857nesppxtjh0pou6p0nt` (`orden_id`),
  CONSTRAINT `FKcmv8857nesppxtjh0pou6p0nt` FOREIGN KEY (`orden_id`) REFERENCES `orden_compra` (`orden_id`),
  CONSTRAINT `FKsjpu6m25y2h2yleytjkp4igo3` FOREIGN KEY (`codigo_ingrediente`) REFERENCES `ingrediente` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orden_ingrediente`
--

LOCK TABLES `orden_ingrediente` WRITE;
/*!40000 ALTER TABLE `orden_ingrediente` DISABLE KEYS */;
INSERT INTO `orden_ingrediente` VALUES (1,1),(2,2);
/*!40000 ALTER TABLE `orden_ingrediente` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-18  2:32:34
