DELIMITER $$

DROP PROCEDURE IF EXISTS `vasja_caja_prod`.`recalculateSaldo`$$
CREATE DEFINER=`vasja`@`%` PROCEDURE `recalculateSaldo`(IN p_ctaId bigint, IN p_fechamin VARCHAR(255))
  BEGIN
	DECLARE done INT DEFAULT 0;
    DECLARE operId bigint;
    DECLARE v_pen DECIMAL(20,2);
    DECLARE v_usd DECIMAL(20,2);
	DECLARE varIsPen tinyint;
	DECLARE varMontoPen DECIMAL(20,2);
	DECLARE varMontoUsd DECIMAL(20,2);
    DECLARE cursorCtas CURSOR FOR SELECT `PEN`,`USD` FROM `CUENTA` WHERE `ID` = p_ctaId;
    DECLARE cursorOpers CURSOR FOR SELECT `ID`,`ISPEN`, `PEN`, `USD` FROM `operacion` WHERE `CUENTA_ID` = p_ctaId AND `FECHA` >= p_fechamin ORDER BY `FECHA` ASC,`ID` ASC;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
	  
    SELECT `SALDOPEN`, `SALDOUSD` FROM `operacion` WHERE `CUENTA_ID` = p_ctaId AND 
      `FECHA` < p_fechamin ORDER BY `FECHA` DESC LIMIT 1 INTO v_pen, v_usd;
    IF v_pen IS NULL THEN  
      SET v_pen=0.00;
    END IF;
    IF v_usd IS NULL THEN  
      SET v_usd=0.00;
    END IF;
	OPEN cursorOpers;                  
	    read_loop: LOOP
	      SET done = FALSE;	
	      FETCH cursorOpers INTO operId, varIsPen, varMontoPen ,varMontoUsd;                           
	      IF done THEN
        	LEAVE read_loop;
	      END IF;
	      IF (varIsPen=1) THEN                                         
		  SET v_pen=v_pen+varMontoPen;
		  update `CUENTA` set `PEN`=v_pen where `ID`=p_ctaId; 
		  update `operacion` set `SALDOPEN`=v_pen where `ID`= operId;
	      ELSE
		  SET v_usd=v_usd+varMontoUsd;
		  update `CUENTA` set `USD`=v_usd where `ID`=p_ctaId;    
		  update `operacion` set `SALDOUSD`=v_usd where `ID`=operId;
	      END IF;                            
	    END LOOP;
    CLOSE cursorOpers;
  END$$
DELIMITER ;
