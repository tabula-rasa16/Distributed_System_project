package com.flightinfosys.mapper;

import com.flightinfosys.domain.Flight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FlightMapper {

    List<Integer> selectFlightId(Flight flight);

    Flight selectFlightInfoById(int id);
    
    // 根据 flightId 返回航班的可用座位数
    int checkFlightAvailability(int flightId);

    // 更新航班的可用座位数
    int updateSeatAvailability(@Param("flightId") int flightId, @Param("seatCount") int seatCount);

    // 根据最大票价查询符合条件的航班
    List<Flight> selectFlightsByMaxPrice(@Param("maxPrice") long maxPrice);

    // 查询符合条件的最便宜的航班
    Flight selectCheapestFlightWithSeats(@Param("source") String source, @Param("destination") String destination);


}
