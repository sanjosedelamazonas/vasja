DELIMITER $$

DROP PROCEDURE IF EXISTS `calculateSaldos`$$
CREATE DEFINER=`vasja`@`%` PROCEDURE `calculateSaldos`()
BEGIN
        DECLARE ctaId bigint DEFAULT 0;
        DECLARE done INT DEFAULT 0;
        DECLARE operId bigint;
        DECLARE v_pen DECIMAL;
        DECLARE v_usd DECIMAL;
        DECLARE v_saldoPen DECIMAL;
        DECLARE v_saldoUsd DECIMAL;
		
        DECLARE cursorCtas CURSOR FOR SELECT `id`,`pen`,`usd` FROM `cuenta`;
		
        DECLARE cursorOpers CURSOR FOR SELECT `id`,`ispen`, `pen`, `usd` FROM `operacion` WHERE `cuenta_id` = ctaId;
        
		DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
		OPEN cursorCtas;
            REPEAT
                FETCH cursorCtas INTO ctaId, v_pen, v_usd;
                IF NOT done THEN  
                    OPEN cursorOpers;
                            block2: BEGIN                                
                                DECLARE doneOpers INT DEFAULT 0;
                                DECLARE varIsPen tinyint;
                                DECLARE varMontoPen decimal;
                                DECLARE varMontoUsd decimal;
                                DECLARE CONTINUE HANDLER FOR NOT FOUND SET doneOpers = 1;
                                IF v_pen IS NULL THEN  
                                SET v_pen=0;
                                END IF;
                                IF v_usd IS NULL THEN  
                                SET v_usd=0;
                                END IF;
                                SET v_saldoPen=v_pen;
                                SET v_saldoUsd=v_usd;
                                IF NOT doneOpers THEN 
                               REPEAT
                                    FETCH cursorOpers INTO operId, varIsPen, varMontoPen ,varMontoUsd ;
                                    IF (varIsPen=1) THEN                                         
                                        SET v_saldoPen=v_saldoPen+varMontoPen;
                                        -- INSERT INTO `vasja_test`.`lastsaldo` (`FECHA`,`PEN`,`USD`) VALUES (NOW(),v_pen, v_saldoPen);
                                        update `cuenta` set `pen`=v_saldoPen where `id`=ctaId; 
                                        update `operacion` set `SALDOPEN`=v_saldoPen where `id`= operId;
                                    ELSE
                                        SET v_saldoUsd=v_saldoUsd+varMontoUsd;
                                        update `cuenta` set `usd`=v_saldoUsd where `id`=ctaId;    
                                        update `operacion` set `SALDOUSD`=v_saldoUsd where `id`=operId;
                                    END IF;
                            UNTIL doneOpers END REPEAT;
                            END IF;
                            END block2;
                    CLOSE cursorOpers;
                END IF;
            UNTIL done END REPEAT;
    CLOSE cursorCtas;
    END$$

DELIMITER ;


DELIMITER $$

DROP PROCEDURE IF EXISTS `calculateSaldosOperaciones`$$
CREATE DEFINER=`vasja`@`%` PROCEDURE `calculateSaldosOperaciones`()
BEGIN 
        DECLARE ctaId bigint DEFAULT 0;
        DECLARE done INT DEFAULT 0;
        DECLARE operId bigint DEFAULT 0;
        DECLARE cta_pen DECIMAL(20,2);
        DECLARE cta_usd DECIMAL(20,2);
        DECLARE v_saldoPen DECIMAL(20,2);
        DECLARE v_saldoUsd DECIMAL(20,2);
		
        DECLARE cursorCtas CURSOR FOR SELECT `id`,`pen`,`usd` FROM `cuenta`;
		
        DECLARE cursorOpers CURSOR FOR SELECT `id`,`ispen`, `pen`, `usd` FROM `operacion` WHERE `cuenta_id` = ctaId;
        
		DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
		OPEN cursorCtas;
      REPEAT
            FETCH cursorCtas INTO ctaId, cta_pen, cta_usd;
            IF NOT done THEN  
                    IF cta_pen IS NULL THEN  
                        SET cta_pen=0;
                    END IF;
                    
                    IF cta_usd IS NULL THEN  
                        SET cta_usd=0;
                    END IF;
                    
                    SET v_saldoPen=cta_pen;
                    SET v_saldoUsd=cta_usd;
                    
            
                    OPEN cursorOpers;
                            block2: BEGIN                                
                                DECLARE doneOpers INT DEFAULT 0;
                                DECLARE varIsPen tinyint;
                                DECLARE varMontoPen decimal(20,2);
                                DECLARE varMontoUsd decimal(20,2);
                                DECLARE CONTINUE HANDLER FOR NOT FOUND SET doneOpers = 1;
                               WHILE NOT doneOpers DO
                                    FETCH cursorOpers INTO operId, varIsPen, varMontoPen ,varMontoUsd ;
                                    if doneOpers=0 THEN
                                 --       INSERT INTO `lastsaldo` VALUES (NOW(),varIsPen, varMontoPen,varMontoUsd);
                                        SET v_saldoPen=v_saldoPen+varMontoPen;
                                        SET v_saldoUsd=v_saldoUsd+varMontoUsd;        
                                 --       INSERT INTO `lastsaldo` VALUES (NOW(),varMontoPen, v_saldoPen,"");
                                        update `operacion` set `SALDOPEN`=v_saldoPen, `SALDOUSD`=v_saldoUsd where `id`= operId;   
                                        update `cuenta` set `pen`=v_saldoPen,`usd`=v_saldoUsd where `id`=ctaId; 
                                    END IF;
                                END WHILE;
                            END block2;
                    CLOSE cursorOpers;
                    
                END IF;
            UNTIL done END REPEAT;
    CLOSE cursorCtas;
    END$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS `check_consistency`$$
CREATE DEFINER=`vasja`@`%` PROCEDURE `check_consistency`()
BEGIN
	DECLARE v_saldo_pen,v_saldo_usd,
		    v_cuenta_pen,v_cuenta_usd,
		    v_oper_pen,v_oper_usd	DECIMAL(20,2);
	CREATE TABLE IF NOT EXISTS `lastsaldo`  (
	  `FECHA` datetime DEFAULT NULL,
	  `PEN` decimal(38,2) DEFAULT NULL,
	  `USD` decimal(38,2) DEFAULT NULL
	);
	DELETE FROM `lastsaldo`;
	INSERT INTO `lastsaldo`(FECHA, PEN, USD) (SELECT FECHA, EFPEN,EFUSD FROM SALDO ORDER BY FECHA DESC LIMIT 1)
		UNION ALL
		(SELECT FECHA,EFPEN,EFUSD FROM SALDO ORDER BY FECHA DESC LIMIT 1);
	CREATE TABLE IF NOT EXISTS `CONSISTENCY` (
	  `FECHA` datetime DEFAULT NULL,
	  `SALDO_PEN` decimal(38,2) DEFAULT NULL,
	  `SALDO_USD` decimal(38,2) DEFAULT NULL,
	  `cuenta_PEN` decimal(38,2) DEFAULT NULL,
	  `cuenta_USD` decimal(38,2) DEFAULT NULL,
	  `OPER_PEN` decimal(38,2) DEFAULT NULL,
	  `OPER_USD` decimal(38,2) DEFAULT NULL
	);

	SELECT SUM(PEN) FROM `lastsaldo` INTO v_saldo_pen;
	SELECT SUM(USD) FROM `lastsaldo` INTO v_saldo_usd;
	SELECT SUM(PEN) FROM `cuenta` WHERE `cuenta`.`ISCAJA`=0 INTO v_cuenta_pen;
	SELECT SUM(USD) FROM `cuenta` WHERE `cuenta`.`ISCAJA`=0 INTO v_cuenta_usd;
	SELECT SUM(operacion.PEN) FROM `operacion`, `cuenta` WHERE `operacion`.`cuenta_ID`= `cuenta`.`ID` AND `cuenta`.`ISCAJA`=0 INTO v_oper_pen;
	SELECT SUM(operacion.USD) FROM `operacion`, `cuenta` WHERE `operacion`.`cuenta_ID`= `cuenta`.`ID` AND `cuenta`.`ISCAJA`=0 INTO v_oper_usd;

	INSERT INTO CONSISTENCY(FECHA, SALDO_PEN, SALDO_USD, cuenta_PEN, cuenta_USD, OPER_PEN, OPER_USD) 
		VALUES (NOW(), v_saldo_pen, v_saldo_usd, v_cuenta_pen, v_cuenta_usd, v_oper_pen, v_oper_usd);
END$$

DELIMITER ;


DELIMITER $$

DROP PROCEDURE IF EXISTS `check_cuentas_consistency`$$
CREATE DEFINER=`vasja`@`%` PROCEDURE `check_cuentas_consistency`()
BEGIN
   DECLARE done INT DEFAULT 0;
   DECLARE consist INT DEFAULT 0;
	DECLARE v_saldo_pen,v_saldo_usd,
		    v_cuenta_pen,v_cuenta_usd,
		    v_oper_pen,v_oper_usd	DECIMAL(20,2);
   DECLARE v_numero bigint;
   DECLARE v_cuenta_id bigint;
   DECLARE v_nombre VARCHAR(255);
   DECLARE cursorCtas CURSOR FOR SELECT `id`,`numero`,`nombre` FROM `cuenta` ORDER BY NUMERO ASC;
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   CREATE TABLE IF NOT EXISTS `CONSISTENCY_cuentaS`  (
	  `FECHA` datetime DEFAULT NULL,
     `ID` bigint,
     `NUMERO` bigint,
     `NOMBRE` varchar(255),
	  `cuenta_PEN` decimal(38,2) DEFAULT NULL,
	  `cuenta_USD` decimal(38,2) DEFAULT NULL,
     `SUM_OPER_PEN` decimal(38,2) DEFAULT NULL,
	  `SUM_OPER_USD` decimal(38,2) DEFAULT NULL,
     `IS_CONSISTENT` tinyint
	);
	DELETE FROM `CONSISTENCY_cuentaS`;

   OPEN cursorCtas;
    read_loop: LOOP
     SET done = FALSE;
     FETCH cursorCtas INTO v_cuenta_id, v_numero, v_nombre;
     IF done THEN
        LEAVE read_loop;
     END IF;
     IF NOT done THEN
        SET consist = 0;
        SELECT PEN FROM `cuenta` WHERE `cuenta`.`ID`=v_cuenta_id INTO v_cuenta_pen;
        SELECT USD FROM `cuenta` WHERE `cuenta`.`ID`=v_cuenta_id INTO v_cuenta_usd;
        SELECT COALESCE(SUM(operacion.PEN),0,00) FROM `operacion`, `cuenta` WHERE `operacion`.`cuenta_ID`= `cuenta`.`ID` AND `cuenta`.`ID`=v_cuenta_id INTO v_oper_pen;
        SELECT COALESCE(SUM(operacion.USD),0.00) FROM `operacion`, `cuenta` WHERE `operacion`.`cuenta_ID`= `cuenta`.`ID` AND `cuenta`.`ID`=v_cuenta_id INTO v_oper_usd;
        
        IF v_cuenta_pen=v_oper_pen AND v_cuenta_usd=v_oper_usd THEN
            SET consist=1;
        END IF;                        

        INSERT INTO `CONSISTENCY_cuentaS` VALUES (NOW(),v_cuenta_id, v_numero, v_nombre, v_cuenta_pen, v_cuenta_usd, v_oper_pen, v_oper_usd, consist);
     END IF;
    END LOOP;
   CLOSE cursorCtas;
END$$

DELIMITER ;


DELIMITER $$

-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$
DROP PROCEDURE IF EXISTS `cuentas_mensual`$$
CREATE DEFINER=`vasja`@`%` PROCEDURE `cuentas_mensual`(IN p_fechaMin datetime, IN p_fechaMax datetime, IN p_cat_id BIGINT)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_numero bigint;
    DECLARE v_cuenta_id bigint;
    DECLARE v_nombre VARCHAR(255);
	DECLARE v_pen_cargo DECIMAL(20,2);
    DECLARE v_pen_abono DECIMAL(20,2);
    DECLARE v_usd_cargo DECIMAL(20,2);
    DECLARE v_usd_abono DECIMAL(20,2);
    DECLARE v_pen_ant DECIMAL(20,2);
    DECLARE v_usd_ant DECIMAL(20,2);
    DECLARE v_pen_now DECIMAL(20,2);
    DECLARE v_usd_now DECIMAL(20,2);
    DECLARE cursorCtas CURSOR FOR SELECT `id`,`numero`,`nombre` FROM `cuenta` WHERE `CATEGORIAcuenta_ID` = p_cat_id ORDER BY ID ASC;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

CREATE TABLE IF NOT EXISTS `cuenta_MENSUAL`  (
	  `cuenta_NUMERO` BIGINT DEFAULT NULL,
	  `cuenta_NOMBRE` VARCHAR(255) DEFAULT NULL,
	  `PEN_ANTERIOR` decimal(38,2) DEFAULT NULL,
	  `PEN_INGRESOS` decimal(38,2) DEFAULT NULL,
      `PEN_EGRESOS` decimal(38,2) DEFAULT NULL,
      `PEN_FINAL` decimal(38,2) DEFAULT NULL,
      `USD_ANTERIOR` decimal(38,2) DEFAULT NULL,
	  `USD_INGRESOS` decimal(38,2) DEFAULT NULL,
      `USD_EGRESOS` decimal(38,2) DEFAULT NULL,
      `USD_FINAL` decimal(38,2) DEFAULT NULL
);
DELETE FROM `cuenta_MENSUAL`;

OPEN cursorCtas;
   read_loop: LOOP
     SET done = FALSE;
     FETCH cursorCtas INTO v_cuenta_id, v_numero, v_nombre;
     IF done THEN
        LEAVE read_loop;
     END IF;
     IF NOT done THEN
       SET v_pen_cargo = 0;
       SET v_pen_abono = 0;
       SET v_usd_cargo = 0;
       SET v_usd_abono = 0;
       SET v_pen_ant = 0;
       SET v_usd_ant = 0;
       SET v_pen_now = 0;
       SET v_usd_now = 0;
       SELECT
             COALESCE(SUM(operacion.`PEN`),0.00) AS operacion_PEN     
        FROM
             `operacion` operacion INNER JOIN `cuenta` cuenta ON operacion.`cuenta_ID` = cuenta.`ID`     
        WHERE
             operacion.FECHA >= p_fechaMin 
         AND operacion.FECHA < p_fechaMax
         AND operacion.cuenta_ID = v_cuenta_id
         AND operacion.`ISCARGO`=1 INTO v_pen_cargo;
        -- IF (v_pen_cargo IS NULL) THEN v_pen_cargo=0.00;
        SELECT
             COALESCE(SUM(operacion.`PEN`),0.00) AS operacion_PEN     
        FROM
             `operacion` operacion INNER JOIN `cuenta` cuenta ON operacion.`cuenta_ID` = cuenta.`ID`     
        WHERE
             operacion.FECHA >= p_fechaMin 
         AND operacion.FECHA < p_fechaMax
         AND operacion.cuenta_ID = v_cuenta_id
         AND operacion.`ISCARGO`=0 INTO v_pen_abono;
        SELECT
             COALESCE(SUM(operacion.`USD`),0.00)     
        FROM
             `operacion` operacion INNER JOIN `cuenta` cuenta ON operacion.`cuenta_ID` = cuenta.`ID`     
        WHERE
             operacion.FECHA >= p_fechaMin 
         AND operacion.FECHA < p_fechaMax
         AND operacion.cuenta_ID = v_cuenta_id
         AND operacion.`ISCARGO`=1 INTO v_usd_cargo;
        SELECT
             COALESCE(SUM(operacion.`USD`),0.00)
        FROM
             `operacion` operacion INNER JOIN `cuenta` cuenta ON operacion.`cuenta_ID` = cuenta.`ID`     
        WHERE
             operacion.FECHA >= p_fechaMin 
         AND operacion.FECHA < p_fechaMax
         AND operacion.cuenta_ID = v_cuenta_id
         AND operacion.`ISCARGO`=0 INTO v_usd_abono;
        
        -- ANTERIOR
        SELECT
             COALESCE(operacion.`SALDOPEN`,0.00)
        FROM
             `operacion` operacion INNER JOIN `cuenta` cuenta ON operacion.`cuenta_ID` = cuenta.`ID`
        WHERE
             operacion.FECHA < p_fechaMin
         AND operacion.cuenta_ID = v_cuenta_id
        ORDER BY operacion.FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_pen_ant;

        SELECT
             COALESCE(operacion.`SALDOUSD`,0.00)
        FROM
             `operacion` operacion INNER JOIN `cuenta` cuenta ON operacion.`cuenta_ID` = cuenta.`ID`
        WHERE
             operacion.FECHA < p_fechaMin
         AND operacion.cuenta_ID = v_cuenta_id
        ORDER BY operacion.FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_usd_ant;

        -- ACTUAL
        SELECT
             COALESCE(operacion.`SALDOPEN`,0.00)
        FROM
             `operacion` operacion INNER JOIN `cuenta` cuenta ON operacion.`cuenta_ID` = cuenta.`ID`
        WHERE
             operacion.FECHA < p_fechaMax
         AND operacion.cuenta_ID = v_cuenta_id
        ORDER BY operacion.FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_pen_now;

        SELECT
             COALESCE(operacion.`SALDOUSD`,0.00)
        FROM
             `operacion` operacion INNER JOIN `cuenta` cuenta ON operacion.`cuenta_ID` = cuenta.`ID`
        WHERE
             operacion.FECHA < p_fechaMax
         AND operacion.cuenta_ID = v_cuenta_id
        ORDER BY operacion.FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_usd_now;

        INSERT INTO `cuenta_MENSUAL` VALUES(v_numero, v_nombre, v_pen_ant, v_pen_abono, v_pen_cargo, v_pen_now, v_usd_ant, v_usd_abono, v_usd_cargo, v_usd_now);
      END IF;
    END LOOP;
CLOSE cursorCtas;
END$$
DELIMITER ;

DELIMITER $$

DELIMITER $$

-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$
DROP PROCEDURE IF EXISTS `saldosForFecha`$$
CREATE DEFINER=`vasja`@`%` PROCEDURE `saldosForFecha`(IN p_fecha_min VARCHAR(255), IN p_fecha_max VARCHAR(255))
BEGIN
   DECLARE done INT DEFAULT 0;
	DECLARE v_catId bigint;
   DECLARE v_nombre VARCHAR(255);
   DECLARE v_pen DECIMAL(20,2);
   DECLARE v_usd DECIMAL(20,2);
   DECLARE v_fecha DATETIME;
   DECLARE cursorCats CURSOR FOR    
    SELECT ID, NOMBRE FROM CATEGORIAcuenta WHERE NOMBRE NOT LIKE 'Caja y Bancos' AND NOMBRE NOT LIKE 'Caja y BANCOs';
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   CREATE TABLE IF NOT EXISTS `saldosDiario` (
        `fecha` DATETIME,
        `PEN` DECIMAL(20,2),
        `USD` DECIMAL(20,2),
		`CAT_ID` bigint, 
        `NOMBRE` VARCHAR(255),
        `IS_INICIAL` TINYINT
    );
    DELETE FROM `saldosDiario`;
    OPEN cursorCats;                   
	    read_loop: LOOP
	      SET done = FALSE;	
	      FETCH cursorCats INTO v_catId, v_nombre;                           
	      IF done THEN
            LEAVE read_loop;
	      END IF;
           SET v_pen = 0.00;
           SET v_usd = 0.00;
           SELECT operacion.FECHA,COALESCE(operacion.SALDOPEN,0.00), COALESCE(operacion.SALDOUSD,0.00) FROM operacion, cuenta, CATEGORIAcuenta 
			 WHERE operacion.cuenta_ID = cuenta.ID AND CATEGORIAcuenta.CAJAcuenta_ID = cuenta.ID AND CATEGORIAcuenta.ID = v_catId 
			 AND `operacion`.`FECHA` < p_fecha_min ORDER BY FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_fecha, v_pen, v_usd; 
           -- SELECT SALDO.FECHA,COALESCE(SALDO.EFPEN,0.00), COALESCE(SALDO.EFUSD,0.00) FROM SALDO WHERE SALDO.CATEGORIAcuenta_ID=v_catId AND `SALDO`.`FECHA` < p_fecha_min 
           -- ORDER BY fecha DESC LIMIT 1 INTO v_fecha, v_pen, v_usd;
           INSERT INTO `saldosDiario`(`fecha`,`PEN`,`USD`,`CAT_ID`,`NOMBRE`, `IS_INICIAL`) VALUES (v_fecha, v_pen, v_usd,v_catId, v_nombre,1);		  
           SET v_pen = 0.00;
           SET v_usd = 0.00;           
           SELECT operacion.FECHA,COALESCE(operacion.SALDOPEN,0.00), COALESCE(operacion.SALDOUSD,0.00) FROM operacion, cuenta, CATEGORIAcuenta 
			 WHERE operacion.cuenta_ID = cuenta.ID AND CATEGORIAcuenta.CAJAcuenta_ID = cuenta.ID AND CATEGORIAcuenta.ID = v_catId 
			 AND `operacion`.`FECHA` < p_fecha_max ORDER BY FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_fecha, v_pen, v_usd; 
--           SELECT SALDO.FECHA,COALESCE(SALDO.EFPEN,0.00), COALESCE(SALDO.EFUSD,0.00) FROM SALDO WHERE SALDO.CATEGORIAcuenta_ID=v_catId AND `SALDO`.`FECHA` < p_fecha_max 
 --           ORDER BY fecha DESC LIMIT 1 INTO v_fecha, v_pen, v_usd;
           INSERT INTO `saldosDiario`(`fecha`,`PEN`,`USD`,`CAT_ID`,`NOMBRE`, `IS_INICIAL`) VALUES (v_fecha, v_pen, v_usd,v_catId, v_nombre,0);		  
	    END LOOP;
     CLOSE cursorCats;
END$$
DELIMITER ;

DROP VIEW IF EXISTS `cc_view`;
CREATE VIEW `cc_view` AS (
select `det`.`ID` AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentaCENTROCOSTO_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		`parent2`.`ID` AS `parent2_ID`,
		`parent2`.`CODIGO` AS `parent2_CODIGO`,
		`parent2`.`ISLEAF` AS `parent2_ISLEAF`,
		`parent2`.`NOMBRE` AS `parent2_NOMBRE`,
		`parent2`.`LEVEL` AS `parent2_LEVEL`,
		`det`.`ID` AS `det_ID`,
		`det`.`CODIGO` AS `det_CODIGO`,
		`det`.`ISLEAF` AS `det_ISLEAF`,
		`det`.`NOMBRE` AS `det_NOMBRE`,
		`det`.`LEVEL` AS `det_LEVEL` 
		from 
		(((`CENTROCOSTO` `parent0` left join `CENTROCOSTO` `parent1` 
			on((`parent0`.`ID` = `parent1`.`CATEGORIACENTROCOSTO_ID`))) 
					left join `CENTROCOSTO` `parent2` on((`parent1`.`ID` = `parent2`.`CATEGORIACENTROCOSTO_ID`))) 
				left join `CENTROCOSTO` `det` on((`parent2`.`ID` = `det`.`CATEGORIACENTROCOSTO_ID`)))
		where ((`det`.`LEVEL` = '3') and (`det`.`ISLEAF` = '1'))) 
union (
		select NULL AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentaCENTROCOSTO_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		`parent2`.`ID` AS `parent2_ID`,
		`parent2`.`CODIGO` AS `parent2_CODIGO`,
		`parent2`.`ISLEAF` AS `parent2_ISLEAF`,
		`parent2`.`NOMBRE` AS `parent2_NOMBRE`,
		`parent2`.`LEVEL` AS `parent2_LEVEL`,
		`det`.`ID` AS `det_ID`,
		`det`.`CODIGO` AS `det_CODIGO`,
		`det`.`ISLEAF` AS `det_ISLEAF`,
		`det`.`NOMBRE` AS `det_NOMBRE`,
		`det`.`LEVEL` AS `det_LEVEL` from (((`CENTROCOSTO` `parent0` left join `CENTROCOSTO` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIACENTROCOSTO_ID`))) left join `CENTROCOSTO` `parent2` on((`parent1`.`ID` = `parent2`.`CATEGORIACENTROCOSTO_ID`))) left join `CENTROCOSTO` `det` on((`parent2`.`ID` = `det`.`CATEGORIACENTROCOSTO_ID`))) where ((`det`.`LEVEL` = '3') and (`det`.`ISLEAF` = '0'))) union (select `parent2`.`ID` AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentaCENTROCOSTO_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		`parent2`.`ID` AS `parent2_ID`,
		`parent2`.`CODIGO` AS `parent2_CODIGO`,
		`parent2`.`ISLEAF` AS `parent2_ISLEAF`,
		`parent2`.`NOMBRE` AS `parent2_NOMBRE`,
		`parent2`.`LEVEL` AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from ((`CENTROCOSTO` `parent0` left join `CENTROCOSTO` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIACENTROCOSTO_ID`))) left join `CENTROCOSTO` `parent2` on((`parent1`.`ID` = `parent2`.`CATEGORIACENTROCOSTO_ID`))) where ((`parent2`.`LEVEL` = '2') and (`parent2`.`ISLEAF` = '1'))) 
		union (select NULL AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentaCENTROCOSTO_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		`parent2`.`ID` AS `parent2_ID`,
		`parent2`.`CODIGO` AS `parent2_CODIGO`,
		`parent2`.`ISLEAF` AS `parent2_ISLEAF`,
		`parent2`.`NOMBRE` AS `parent2_NOMBRE`,
		`parent2`.`LEVEL` AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from ((`CENTROCOSTO` `parent0` left join `CENTROCOSTO` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIACENTROCOSTO_ID`))) left join `CENTROCOSTO` `parent2` on((`parent1`.`ID` = `parent2`.`CATEGORIACENTROCOSTO_ID`))) where ((`parent2`.`LEVEL` = '2') and (`parent2`.`ISLEAF` = '0'))) 
		union (select `parent1`.`ID` AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentaCENTROCOSTO_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		NULL AS `parent2_ID`,
		NULL AS `parent2_CODIGO`,
		NULL AS `parent2_ISLEAF`,
		NULL AS `parent2_NOMBRE`,
		NULL AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from (`CENTROCOSTO` `parent0` left join `CENTROCOSTO` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIACENTROCOSTO_ID`))) where ((`parent1`.`LEVEL` = '1') and (`parent1`.`ISLEAF` = '1'))) 
		union (select NULL AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentaCENTROCOSTO_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		NULL AS `parent2_ID`,
		NULL AS `parent2_CODIGO`,
		NULL AS `parent2_ISLEAF`,
		NULL AS `parent2_NOMBRE`,
		NULL AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from (`CENTROCOSTO` `parent0` left join `CENTROCOSTO` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIACENTROCOSTO_ID`))) where ((`parent1`.`LEVEL` = '1') and (`parent1`.`ISLEAF` = '0'))) union (select `parent0`.`ID` AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentaCENTROCOSTO_ID` AS `parent0_cuenta`,
		NULL AS `parent1_ID`,
		NULL AS `parent1_CODIGO`,
		NULL AS `parent1_ISLEAF`,
		NULL AS `parent1_NOMBRE`,
		NULL AS `parent1_LEVEL`,
		NULL AS `parent2_ID`,
		NULL AS `parent2_CODIGO`,
		NULL AS `parent2_ISLEAF`,
		NULL AS `parent2_NOMBRE`,
		NULL AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from `CENTROCOSTO` `parent0` where ((`parent0`.`LEVEL` = '0') and (`parent0`.`ISLEAF` = '1'))) union (select NULL AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentaCENTROCOSTO_ID` AS `parent0_cuenta`,
		NULL AS `parent1_ID`,
		NULL AS `parent1_CODIGO`,
		NULL AS `parent1_ISLEAF`,
		NULL AS `parent1_NOMBRE`,
		NULL AS `parent1_LEVEL`,
		NULL AS `parent2_ID`,
		NULL AS `parent2_CODIGO`,
		NULL AS `parent2_ISLEAF`,
		NULL AS `parent2_NOMBRE`,
		NULL AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from `CENTROCOSTO` `parent0` where ((`parent0`.`LEVEL` = '0') and (`parent0`.`ISLEAF` = '0'))) ;
		
DROP VIEW IF EXISTS `rubroproy_view`;
CREATE VIEW `rubroproy_view` AS 
(select `det`.`ID` AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`CUENTARUBROPROYECTO_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		`parent2`.`ID` AS `parent2_ID`,
		`parent2`.`CODIGO` AS `parent2_CODIGO`,
		`parent2`.`ISLEAF` AS `parent2_ISLEAF`,
		`parent2`.`NOMBRE` AS `parent2_NOMBRE`,
		`parent2`.`LEVEL` AS `parent2_LEVEL`,
		`det`.`ID` AS `det_ID`,
		`det`.`CODIGO` AS `det_CODIGO`,
		`det`.`ISLEAF` AS `det_ISLEAF`,
		`det`.`NOMBRE` AS `det_NOMBRE`,
		`det`.`LEVEL` AS `det_LEVEL` 
		from 
		(((`rubroproyecto` `parent0` left join `rubroproyecto` `parent1` 
			on((`parent0`.`ID` = `parent1`.`CATEGORIARUBROPROYECTO_ID`))) 
					left join `rubroproyecto` `parent2` on((`parent1`.`ID` = `parent2`.`CATEGORIARUBROPROYECTO_ID`))) 
				left join `rubroproyecto` `det` on((`parent2`.`ID` = `det`.`CATEGORIARUBROPROYECTO_ID`)))
		where ((`det`.`LEVEL` = '3') and (`det`.`ISLEAF` = '1'))) 
union (
		select NULL AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentarubroproyecto_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		`parent2`.`ID` AS `parent2_ID`,
		`parent2`.`CODIGO` AS `parent2_CODIGO`,
		`parent2`.`ISLEAF` AS `parent2_ISLEAF`,
		`parent2`.`NOMBRE` AS `parent2_NOMBRE`,
		`parent2`.`LEVEL` AS `parent2_LEVEL`,
		`det`.`ID` AS `det_ID`,
		`det`.`CODIGO` AS `det_CODIGO`,
		`det`.`ISLEAF` AS `det_ISLEAF`,
		`det`.`NOMBRE` AS `det_NOMBRE`,
		`det`.`LEVEL` AS `det_LEVEL` 
from (((`rubroproyecto` `parent0` left join `rubroproyecto` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIARUBROPROYECTO_ID`))) 
left join `rubroproyecto` `parent2` on((`parent1`.`ID` = `parent2`.`CATEGORIARUBROPROYECTO_ID`))) 
left join `rubroproyecto` `det` on((`parent2`.`ID` = `det`.`CATEGORIARUBROPROYECTO_ID`))) 
where ((`det`.`LEVEL` = '3') and (`det`.`ISLEAF` = '0'))
) 
union (select `parent2`.`ID` AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentarubroproyecto_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		`parent2`.`ID` AS `parent2_ID`,
		`parent2`.`CODIGO` AS `parent2_CODIGO`,
		`parent2`.`ISLEAF` AS `parent2_ISLEAF`,
		`parent2`.`NOMBRE` AS `parent2_NOMBRE`,
		`parent2`.`LEVEL` AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from ((`rubroproyecto` `parent0` 
left join `rubroproyecto` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIARUBROPROYECTO_ID`))) 
left join `rubroproyecto` `parent2` on((`parent1`.`ID` = `parent2`.`CATEGORIARUBROPROYECTO_ID`))) 
where ((`parent2`.`LEVEL` = '2') and (`parent2`.`ISLEAF` = '1'))) 
union (select NULL AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentarubroproyecto_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		`parent2`.`ID` AS `parent2_ID`,
		`parent2`.`CODIGO` AS `parent2_CODIGO`,
		`parent2`.`ISLEAF` AS `parent2_ISLEAF`,
		`parent2`.`NOMBRE` AS `parent2_NOMBRE`,
		`parent2`.`LEVEL` AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from ((`rubroproyecto` `parent0` 
left join `rubroproyecto` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIARUBROPROYECTO_ID`))) 
left join `rubroproyecto` `parent2` on((`parent1`.`ID` = `parent2`.`CATEGORIARUBROPROYECTO_ID`))) 
where ((`parent2`.`LEVEL` = '2') and (`parent2`.`ISLEAF` = '0'))) 
		union (select `parent1`.`ID` AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentarubroproyecto_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		NULL AS `parent2_ID`,
		NULL AS `parent2_CODIGO`,
		NULL AS `parent2_ISLEAF`,
		NULL AS `parent2_NOMBRE`,
		NULL AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from (`rubroproyecto` `parent0` 
left join `rubroproyecto` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIARUBROPROYECTO_ID`))) 
where ((`parent1`.`LEVEL` = '1') and (`parent1`.`ISLEAF` = '1'))) 
		union (select NULL AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentarubroproyecto_ID` AS `parent0_cuenta`,
		`parent1`.`ID` AS `parent1_ID`,
		`parent1`.`CODIGO` AS `parent1_CODIGO`,
		`parent1`.`ISLEAF` AS `parent1_ISLEAF`,
		`parent1`.`NOMBRE` AS `parent1_NOMBRE`,
		`parent1`.`LEVEL` AS `parent1_LEVEL`,
		NULL AS `parent2_ID`,
		NULL AS `parent2_CODIGO`,
		NULL AS `parent2_ISLEAF`,
		NULL AS `parent2_NOMBRE`,
		NULL AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from (`rubroproyecto` `parent0` 
left join `rubroproyecto` `parent1` on((`parent0`.`ID` = `parent1`.`CATEGORIARUBROPROYECTO_ID`))) 
where ((`parent1`.`LEVEL` = '1') and (`parent1`.`ISLEAF` = '0'))) 
union (select `parent0`.`ID` AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentarubroproyecto_ID` AS `parent0_cuenta`,
		NULL AS `parent1_ID`,
		NULL AS `parent1_CODIGO`,
		NULL AS `parent1_ISLEAF`,
		NULL AS `parent1_NOMBRE`,
		NULL AS `parent1_LEVEL`,
		NULL AS `parent2_ID`,
		NULL AS `parent2_CODIGO`,
		NULL AS `parent2_ISLEAF`,
		NULL AS `parent2_NOMBRE`,
		NULL AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from `rubroproyecto` `parent0` 
where ((`parent0`.`LEVEL` = '0') and (`parent0`.`ISLEAF` = '1'))) 
union (select NULL AS `id`,
		`parent0`.`ID` AS `parent0_ID`,
		`parent0`.`CODIGO` AS `parent0_CODIGO`,
		`parent0`.`ISLEAF` AS `parent0_ISLEAF`,
		`parent0`.`NOMBRE` AS `parent0_NOMBRE`,
		`parent0`.`LEVEL` AS `parent0_LEVEL`,
		`parent0`.`cuentarubroproyecto_ID` AS `parent0_cuenta`,
		NULL AS `parent1_ID`,
		NULL AS `parent1_CODIGO`,
		NULL AS `parent1_ISLEAF`,
		NULL AS `parent1_NOMBRE`,
		NULL AS `parent1_LEVEL`,
		NULL AS `parent2_ID`,
		NULL AS `parent2_CODIGO`,
		NULL AS `parent2_ISLEAF`,
		NULL AS `parent2_NOMBRE`,
		NULL AS `parent2_LEVEL`,
		NULL AS `det_ID`,
		NULL AS `det_CODIGO`,
		NULL AS `det_ISLEAF`,
		NULL AS `det_NOMBRE`,
		NULL AS `det_LEVEL` from `rubroproyecto` `parent0` where ((`parent0`.`LEVEL` = '0') and (`parent0`.`ISLEAF` = '0')))