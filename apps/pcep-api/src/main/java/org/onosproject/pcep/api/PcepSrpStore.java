package org.onosproject.pcep.api;

public interface PcepSrpStore {

    /**
     * Returns MDSC/PNC/report SRP id mapping.
     *
     * @param pathName symbolic path name of the LSP
     * @return SRP id mapping
     */
    SrpIdMapping getSrpIdMapping(String pathName);

    /**
     * Adds a new SRP id mapping between MDSC and PNC generated ids.
     *
     * @param pathName symbolic path name of the LSP
     * @param srpIdMapping the mapping information (full/partial)
     */
    void addSrpIdMapping(String pathName, SrpIdMapping srpIdMapping);

    /**
     * Updates the SRP id mapping between MDSC and PNC generated ids.
     *
     * @param pathName symbolic path name of the LSP
     * @param srpIdMapping the updated mapping information
     */
    void updateSrpIdMapping(String pathName, SrpIdMapping srpIdMapping);

    /**
     * Removes MDSC/PNC/report SRP id mapping.
     *
     * @param pathName symbolic path name of the LSP
     */
    void removeSrpIdMapping(String pathName);
}
