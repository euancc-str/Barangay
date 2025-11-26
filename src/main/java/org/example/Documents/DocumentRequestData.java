package org.example.Documents;

import org.example.Users.Resident;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DocumentRequestData {
    private static DocumentRequestData instance;
    private List<DocumentRequest> documentRequest;
    private DocumentRequestData(){
        documentRequest = new ArrayList<>();
    }
    public static DocumentRequestData getInstance(){
        if(instance == null){
            instance = new DocumentRequestData();
        }
        return instance;
    }

    public void addDocument(DocumentRequest dr) {
        documentRequest.add(dr);
        System.out.println("Resident: " + dr.getUsername() +"\n" +dr.getPurpose());
    }

    private String residentName(Resident r){
        return r.getFirstName() + " " + (r.getMiddleName() != null ? r.getMiddleName() + " " : "");
    }


    public DocumentRequest collectDataFromResident(Resident r,String purpose) {
        return DocumentRequest.builder()
                .sex(r.getSex())
                .name(residentName(r))
                .lastName(r.getLastName())
                .suffix(r.getSuffix())
                .age(r.getAge())
                .dob(r.getDob())
                .civilStatus(r.getCivilStatus())
                .address(r.getAddress())
                .requestDate(LocalDateTime.now())
                .purpose(purpose)
                .residentId(r.getResidentId())
                .requestDate(LocalDateTime.now())
                .purpose(purpose)
                .status("Pending")
                .paid(false)
                .paymentStatus("Unpaid")
                .build();
    }//return to tas pasa sa new variable then collect display sa labas

    private void Test(){
        DocumentRequest r = collectDataFromResident(new Resident(),"Purposeee???");
        DocumentRequest getDataFromBackEnd = new DocumentRequest();//dito tayo ma collect ng data
        DocumentRequest showFullDocumentData = collectDataFromDocument(r,getDataFromBackEnd);
        System.out.println(showFullDocumentData);
    }
    public DocumentRequest collectDataFromDocument(DocumentRequest dr,DocumentRequest getDataFromBackEnd){
        return dr.toBuilder()
                .paid(getDataFromBackEnd.isPaid())
                .status(getDataFromBackEnd.getStatus())
                .build();
    }
}
