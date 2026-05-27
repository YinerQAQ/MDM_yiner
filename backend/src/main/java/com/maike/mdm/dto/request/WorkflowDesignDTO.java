package com.maike.mdm.dto.request;

import com.maike.mdm.entity.MdmWorkflowEdge;
import com.maike.mdm.entity.MdmWorkflowNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDesignDTO {

    private List<MdmWorkflowNode> nodes;
    private List<MdmWorkflowEdge> edges;
}
