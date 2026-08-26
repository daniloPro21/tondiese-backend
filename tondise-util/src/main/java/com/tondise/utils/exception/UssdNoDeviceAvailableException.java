package com.tondise.utils.exception;

/**
 * Aucun appareil disponible pour prendre l'ordre — levée par
 *  après épuisement de ses tentatives.
 *
 * <p>Distincte des autres échecs : aucun menu USSD n'a été composé, rien n'a
 * été soumis à l'opérateur. Un rejeu est donc sûr, contrairement à un ordre
 * resté sans {@code CONFIRM} après envoi, où l'opération a peut-être déjà eu
 * lieu — voir {@code UssdReconciliationJob}, qui refuse au contraire tout
 * rejeu automatique pour ce second cas.</p>
 *
 * <p>Type dédié pour que {@code TransferConsumer} puisse la distinguer d'un
 * bug réel (signature, configuration...) et la router vers la file de reprise
 * différée plutôt que vers un échec immédiat.</p>
 */
public class UssdNoDeviceAvailableException extends BadRequestException {
    public UssdNoDeviceAvailableException(String message) {
        super(message);
    }
}
