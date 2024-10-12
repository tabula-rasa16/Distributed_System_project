package com.flightinfosys.server.service.Impl;


import com.flightinfosys.server.domain.Flight;
import com.flightinfosys.server.mapper.FlightMapper;
import com.flightinfosys.server.service.IFlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class FlightService  implements IFlightService {


//    String resource = "Mybatis-config";
//    InputStream inputStream = Resources.getResourceAsStream(resource);
//    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
//    SqlSession sqlSession = sqlSessionFactory.openSession();
//    private Mapper mapper = sqlSession.getMapper(Mapper.class);

    @Autowired
    private FlightMapper mapper;


    public List<Integer> selectFlightId(Flight flight) {
        return mapper.selectFlightId(flight);
    }


}
