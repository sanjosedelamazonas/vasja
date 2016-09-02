-- --------------------------------------------------------------------------------
-- Routine DDL
-- --------------------------------------------------------------------------------
DELIMITER $$

CREATE DEFINER=`vasja`@`%` PROCEDURE `check_consistency`()
BEGIN
	DECLARE v_saldo_pen,v_saldo_usd,
		    v_cuenta_pen,v_cuenta_usd,
		    v_oper_pen,v_oper_usd	DECIMAL(20,2);
	CREATE TEMPORARY TABLE IF NOT EXISTS `lastsaldo`  (
	  `FECHA` datetime DEFAULT NULL,
	  `PEN` decimal(20,2) DEFAULT NULL,
	  `USD` decimal(20,2) DEFAULT NULL
	);
	DELETE FROM `lastsaldo`;
	INSERT INTO `lastsaldo` (SELECT PEN,USD,FECHA FROM saldo WHERE ISEFECTIVO=0 ORDER BY FECHA DESC LIMIT 1)
		UNION ALL
		(SELECT PEN,USD,FECHA FROM saldo WHERE ISEFECTIVO=1 ORDER BY FECHA DESC LIMIT 1);
	CREATE TABLE IF NOT EXISTS `CONSISTENCY` (
	  `FECHA` datetime DEFAULT NULL,
	  `SALDO_PEN` decimal(20,2) DEFAULT NULL,
	  `SALDO_USD` decimal(20,2) DEFAULT NULL,
	  `cuenta_PEN` decimal(20,2) DEFAULT NULL,
	  `cuenta_USD` decimal(20,2) DEFAULT NULL,
	  `OPER_PEN` decimal(20,2) DEFAULT NULL,
	  `OPER_USD` decimal(20,2) DEFAULT NULL
	);

	SELECT SUM(PEN) FROM `lastsaldo` INTO v_saldo_pen;
	SELECT SUM(USD) FROM `lastsaldo` INTO v_saldo_usd;
	SELECT SUM(PEN) FROM `cuenta` INTO v_cuenta_pen;
	SELECT SUM(USD) FROM `cuenta` INTO v_cuenta_usd;
	SELECT SUM(PEN) FROM `operacion` INTO v_oper_pen;
	SELECT SUM(USD) FROM `operacion` INTO v_oper_usd;

	INSERT INTO CONSISTENCY(FECHA, SALDO_PEN, SALDO_USD, CUENTA_PEN, CUENTA_USD, OPER_PEN, OPER_USD) 
		VALUES (NOW(), v_saldo_pen, v_saldo_usd, v_cuenta_pen, v_cuenta_usd, v_oper_pen, v_oper_usd);
END