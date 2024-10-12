package com.flightinfosys.service.impl;

import com.flightinfosys.domain.Flight;
import com.flightinfosys.mapper.FlightMapper;
import com.flightinfosys.service.IFlightService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class FlightService implements IFlightService{



    private FlightMapper flightMapper;


    public FlightService() {
        String resource = "mybatis-config.xml";

        try{
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession sqlSession = sqlSessionFactory.openSession();
            this.flightMapper = sqlSession.getMapper(FlightMapper.class);
        } catch(IOException e) {
            e.printStackTrace();
        }


    }


    public List<Integer> selectFlightId(Flight flight) {
        return flightMapper.selectFlightId(flight);
    }


}
