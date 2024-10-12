CREATE DATABASE `flight_info_sys` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;

-- flight_info_sys.flight_info definition

CREATE TABLE `flight_info` (
  `id` int NOT NULL,
  `source` varchar(100) NOT NULL,
  `destination` varchar(100) NOT NULL,
  `departure_time` datetime NOT NULL,
  `airfare` float NOT NULL,
  `availabe_seat_num` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;


INSERT INTO flight_info_sys.flight_info (id,source,destination,departure_time,airfare,availabe_seat_num) VALUES
	 (123,'Singapore','London','2024-09-16 11:30:19',114.5,60),
	 (55,'Singapore','London','2024-10-09 23:59:59',66.62,100);
