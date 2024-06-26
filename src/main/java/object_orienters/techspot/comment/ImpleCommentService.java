package object_orienters.techspot.comment;

import object_orienters.techspot.DataTypeUtils;
import object_orienters.techspot.content.ContentNotFoundException;
import object_orienters.techspot.content.ReactableContent;
import object_orienters.techspot.content.ReactableContentRepository;
import object_orienters.techspot.postTypes.DataType;
import object_orienters.techspot.postTypes.DataTypeRepository;
import object_orienters.techspot.profile.Profile;
import object_orienters.techspot.profile.ProfileRepository;
import object_orienters.techspot.reaction.Reaction;
import object_orienters.techspot.reaction.ReactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ImpleCommentService implements CommentService {
    private final CommentRepository commentRepository;
    private final ReactableContentRepository contentRepository;
    private final ProfileRepository profileRepository;
    private final DataTypeRepository dataTypeRepository;
    private final ReactionRepository reactionRepository;

    public ImpleCommentService(CommentRepository commentRepository, ReactableContentRepository contentRepository,
            ProfileRepository profileRepository, DataTypeRepository dataTypeRepository,
            ReactionRepository reactionRepository) {
        this.commentRepository = commentRepository;
        this.contentRepository = contentRepository;
        this.profileRepository = profileRepository;
        this.dataTypeRepository = dataTypeRepository;
        this.reactionRepository = reactionRepository;
    }

    @Override
    @Transactional
    public Comment addComment(Long contentId, String username, MultipartFile file, String text)
            throws ContentNotFoundException, IOException {
        ReactableContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(contentId));
        Profile prof = profileRepository.findById(username).get();
        DataType comment = new DataType();
        if (file != null && !file.isEmpty()) {
            comment.setData(DataTypeUtils.compress(file.getBytes()));
            comment.setType(file.getContentType());
        }
        comment.setType(comment.getType() != null ? comment.getType() : "text/plain");
        comment.setData(comment.getData() != null ? comment.getData() : new byte[10]);
        Comment newComment = new Comment(comment, prof, content);
        newComment.setTextData(text != null ? text : "");
        content.setNumOfComments(content.getNumOfComments() + 1);
        content.getComments().add(newComment);
        dataTypeRepository.save(comment);
        commentRepository.save(newComment);
        contentRepository.save(content);
        return newComment;
    }

    @Override
    public Comment getComment(Long commentId) throws ContentNotFoundException {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ContentNotFoundException(commentId));
    }

    @Override
    public List<Comment> getComments(Long contentId) throws ContentNotFoundException {
        ReactableContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(contentId));
        return content.getComments();
    }

    @Override
    @Transactional
    public void deleteComment(Long contentId, Long commentId) throws ContentNotFoundException {
        ReactableContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(contentId));
        Comment com = commentRepository.findById(commentId).get();
        DataType media = com.getMediaData();
        com.setMediaData(null);
        List<Reaction> reactions = com.getReactions();
        List<Comment> comments = com.getComments();
        com.setReactions(new ArrayList<>());
        com.setComments(new ArrayList<>());
        com.setContentAuthor(null);
        com.setCommentedOn(null);
        content.getComments().removeIf(c -> c.getContentID().equals(commentId));
        content.setNumOfComments(content.getNumOfComments() - 1);
        commentRepository.delete(commentRepository.findById(commentId).get());
        dataTypeRepository.delete(media);
        reactions.stream().forEach(reaction -> {
            Profile prof = reaction.getReactor();
            reaction.setContent(null);
            reaction.setReactor(null);
            reactionRepository.delete(reaction);
            profileRepository.save(prof);
        });
        comments.stream().forEach(comment -> {
            Profile prof = comment.getContentAuthor();
            comment.setCommentedOn(null);
            comment.setContentAuthor(null);
            commentRepository.delete(comment);
            profileRepository.save(prof);
        });

        contentRepository.save(content);

    }

    @Override

    public Comment updateComment(Long contentID, Long commentID, MultipartFile file, String text)
            throws ContentNotFoundException, CommentNotFoundException, IOException {
        contentRepository.findById(contentID).orElseThrow(() -> new ContentNotFoundException(contentID));
        Comment comment = commentRepository.findById(commentID)
                .orElseThrow(() -> new CommentNotFoundException(commentID));
        if (file != null && !file.isEmpty()) {
            comment.getMediaData().setData(DataTypeUtils.compress(file.getBytes()));
            comment.getMediaData().setType(file.getContentType());
        }
        comment.setTextData(text == null ? "" : text);
        return commentRepository.save(comment);
    }

    public boolean isCommentAuthor(String username, Long commentID) {
        Optional<Comment> commentOptional = commentRepository.findById(commentID);
        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            return comment.getContentAuthor().getUsername().equals(username);
        }
        return false;
    }

}
